[CmdletBinding()]
param([string]$ArchivoPropiedades)

$ErrorActionPreference = 'Stop'
$raiz = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$destino = Join-Path $raiz '.env.docker'
if (Test-Path -LiteralPath $destino) {
    throw '.env.docker ya existe. Se conserva sin cambios; revisalo antes de continuar.'
}

# Decodifica escapes de Java Properties. Nunca imprime sus valores.
function Convertir-EscapeJava([string]$texto, [string]$referencia) {
    $resultado = New-Object Text.StringBuilder
    for ($i = 0; $i -lt $texto.Length; $i++) {
        $caracter = $texto[$i]
        if ($caracter -ne '\') { [void]$resultado.Append($caracter); continue }
        $i++
        if ($i -ge $texto.Length) {
            throw "No se admiten lineas continuadas en $referencia; usa una variable de entorno."
        }
        switch -CaseSensitive ($texto[$i]) {
            't' { [void]$resultado.Append("`t") }
            'n' { [void]$resultado.Append("`n") }
            'r' { [void]$resultado.Append("`r") }
            'f' { [void]$resultado.Append([char]12) }
            'u' {
                if ($i + 4 -ge $texto.Length) { throw "Escape Unicode incompleto en $referencia." }
                $hex = $texto.Substring($i + 1, 4)
                if ($hex -notmatch '^[0-9A-Fa-f]{4}$') { throw "Escape Unicode invalido en $referencia." }
                [void]$resultado.Append([char][Convert]::ToInt32($hex, 16))
                $i += 4
            }
            default { [void]$resultado.Append($texto[$i]) }
        }
    }
    return $resultado.ToString()
}

$propiedades = @{}
if ($ArchivoPropiedades) {
    if (-not [IO.Path]::IsPathRooted($ArchivoPropiedades)) {
        $ArchivoPropiedades = Join-Path $raiz $ArchivoPropiedades
    }
    if (-not (Test-Path -LiteralPath $ArchivoPropiedades -PathType Leaf)) {
        throw 'No existe el archivo indicado con -ArchivoPropiedades.'
    }
    $archivos = @($ArchivoPropiedades)
} else {
    # La configuracion local se superpone a la configuracion del classpath.
    $archivos = @('src/main/resources/application.properties',
                  'src/main/resources/application-local.properties', 'application-local.properties') |
        ForEach-Object { Join-Path $raiz $_ }
}
foreach ($archivo in $archivos) {
    if (-not (Test-Path -LiteralPath $archivo -PathType Leaf)) { continue }
    $numeroLinea = 0
    foreach ($linea in [IO.File]::ReadAllLines($archivo, [Text.Encoding]::UTF8)) {
        $numeroLinea++
        if ($linea -match '^\s*(?:[#!]|$)') { continue }
        $referencia = ([IO.Path]::GetFileName($archivo)) + ':' + $numeroLinea
        if ($linea -notmatch '^\s*((?:\\.|[^\\:=\s])+)\s*[:=]\s*(.*)$') {
            throw "Formato de propiedad no admitido en $referencia. Usa clave=valor en una sola linea."
        }
        $nombreSinDecodificar = $Matches[1]
        $valorSinDecodificar = $Matches[2]
        $nombre = Convertir-EscapeJava $nombreSinDecodificar $referencia
        # No recortar espacios finales: Java Properties los conserva en el valor.
        $propiedades[$nombre] = Convertir-EscapeJava $valorSinDecodificar $referencia
    }
}

function Resolver-Propiedad([string]$texto, [string[]]$pila = @()) {
    if ($pila.Count -gt 12) { throw 'Demasiadas referencias entre propiedades.' }
    $patron = '\$\{([^{}]+)\}'
    $tokens = [regex]::Matches($texto, $patron)
    $resto = [regex]::Replace($texto, $patron, '')
    if ($resto.Contains('${')) {
        throw 'Placeholder anidado o incompleto: define su valor mediante una variable de entorno.'
    }
    $resultado = New-Object Text.StringBuilder
    $posicion = 0
    foreach ($token in $tokens) {
        [void]$resultado.Append($texto.Substring($posicion, $token.Index - $posicion))
        $contenido = $token.Groups[1].Value
        $separador = $contenido.IndexOf(':')
        $nombre = $contenido
        $predeterminado = $null
        if ($separador -ge 0) {
            $nombre = $contenido.Substring(0, $separador)
            $predeterminado = $contenido.Substring($separador + 1)
        }
        $resuelto = [Environment]::GetEnvironmentVariable($nombre)
        if ($null -eq $resuelto) {
            if ($propiedades.ContainsKey($nombre)) {
                if ($pila -contains $nombre) { throw "Referencia circular en la propiedad $nombre." }
                $resuelto = Resolver-Propiedad ([string]$propiedades[$nombre]) ($pila + @($nombre))
            } elseif ($null -ne $predeterminado) {
                $resuelto = $predeterminado
            } else {
                throw "Falta la variable o propiedad $nombre; no se genero .env.docker."
            }
        }
        # Los valores de entorno se insertan literalmente, sin reinterpretar sus signos $.
        [void]$resultado.Append([string]$resuelto)
        $posicion = $token.Index + $token.Length
    }
    [void]$resultado.Append($texto.Substring($posicion))
    return $resultado.ToString()
}

function Leer-Valor {
    param([string]$Variable, [string]$Propiedad, [string]$Defecto = '', [switch]$Requerido)
    $valor = [Environment]::GetEnvironmentVariable($Variable)
    if ($null -ne $valor) { return $valor }
    if ($propiedades.ContainsKey($Propiedad)) {
        return Resolver-Propiedad ([string]$propiedades[$Propiedad]) @($Propiedad)
    }
    if ($Requerido) { throw "Falta $Propiedad o $Variable; no se genero .env.docker." }
    return $Defecto
}

function Convertir-HostLoopback([string]$hostConfigurado) {
    if ($hostConfigurado -in @('localhost', '127.0.0.1', '[::1]', '::1')) { return 'host.docker.internal' }
    return $hostConfigurado
}

function Convertir-JdbcLoopback([string]$url) {
    $partes = [regex]::Match($url, '^(jdbc:postgresql://)([^/]+)(/.*)$', [Text.RegularExpressions.RegexOptions]::IgnoreCase)
    if (-not $partes.Success) {
        throw 'SPRING_DATASOURCE_URL debe ser una URL JDBC PostgreSQL con host, puerto opcional y nombre de base.'
    }
    $autoridad = [regex]::Replace($partes.Groups[2].Value,
        '(^|,)(localhost|127\.0\.0\.1|\[::1\])(?=:\d+(?:,|$)|,|$)',
        '${1}host.docker.internal', [Text.RegularExpressions.RegexOptions]::IgnoreCase)
    return $partes.Groups[1].Value + $autoridad + $partes.Groups[3].Value
}

$valores = [ordered]@{
    SPRING_PROFILES_ACTIVE = 'docker'
    SPRING_DATASOURCE_URL = (Convertir-JdbcLoopback (Leer-Valor 'SPRING_DATASOURCE_URL' 'spring.datasource.url' -Requerido))
    SPRING_DATASOURCE_USERNAME = (Leer-Valor 'SPRING_DATASOURCE_USERNAME' 'spring.datasource.username' -Requerido)
    SPRING_DATASOURCE_PASSWORD = (Leer-Valor 'SPRING_DATASOURCE_PASSWORD' 'spring.datasource.password' -Requerido)
    DB_POOL_SIZE = (Leer-Valor 'DB_POOL_SIZE' 'spring.datasource.hikari.maximum-pool-size' '5')
    SPRING_DATA_REDIS_HOST = (Convertir-HostLoopback (Leer-Valor 'SPRING_DATA_REDIS_HOST' 'spring.data.redis.host' 'localhost'))
    SPRING_DATA_REDIS_PORT = (Leer-Valor 'SPRING_DATA_REDIS_PORT' 'spring.data.redis.port' '6379')
    SPRING_DATA_REDIS_USERNAME = (Leer-Valor 'SPRING_DATA_REDIS_USERNAME' 'spring.data.redis.username')
    SPRING_DATA_REDIS_PASSWORD = (Leer-Valor 'SPRING_DATA_REDIS_PASSWORD' 'spring.data.redis.password')
    SPRING_DATA_REDIS_DATABASE = (Leer-Valor 'SPRING_DATA_REDIS_DATABASE' 'spring.data.redis.database' '0')
    JWT_SECRET = (Leer-Valor 'JWT_SECRET' 'jwt.secret' -Requerido)
    DOCUMENTO_CLAVE_CIFRADO = (Leer-Valor 'DOCUMENTO_CLAVE_CIFRADO' 'seguridad.documento.clave-cifrado' -Requerido)
    DOCUMENTO_CLAVE_HMAC = (Leer-Valor 'DOCUMENTO_CLAVE_HMAC' 'seguridad.documento.clave-hmac' -Requerido)
    SMTP_HOST = (Leer-Valor 'SMTP_HOST' 'spring.mail.host' 'localhost')
    SMTP_PORT = (Leer-Valor 'SMTP_PORT' 'spring.mail.port' '587')
    SMTP_USERNAME = (Leer-Valor 'SMTP_USERNAME' 'spring.mail.username')
    SMTP_PASSWORD = (Leer-Valor 'SMTP_PASSWORD' 'spring.mail.password')
    SMTP_AUTH = (Leer-Valor 'SMTP_AUTH' 'spring.mail.properties.mail.smtp.auth' 'true')
    SMTP_STARTTLS = (Leer-Valor 'SMTP_STARTTLS' 'spring.mail.properties.mail.smtp.starttls.enable' 'true')
    SMTP_STARTTLS_REQUIRED = (Leer-Valor 'SMTP_STARTTLS_REQUIRED' 'spring.mail.properties.mail.smtp.starttls.required' 'true')
    STRIPE_SECRET_KEY = (Leer-Valor 'STRIPE_SECRET_KEY' 'STRIPE_SECRET_KEY')
    STRIPE_WEBHOOK_SECRET = (Leer-Valor 'STRIPE_WEBHOOK_SECRET' 'STRIPE_WEBHOOK_SECRET')
    CLOUDINARY_URL = (Leer-Valor 'CLOUDINARY_URL' 'app.storage.cloudinary-url')
    B2_ENABLED = (Leer-Valor 'B2_ENABLED' 'app.auditoria.b2.enabled' 'false')
    B2_ENDPOINT = (Leer-Valor 'B2_ENDPOINT' 'app.auditoria.b2.endpoint')
    B2_REGION = (Leer-Valor 'B2_REGION' 'app.auditoria.b2.region')
    B2_BUCKET = (Leer-Valor 'B2_BUCKET' 'app.auditoria.b2.bucket')
    B2_ACCESS_KEY = (Leer-Valor 'B2_ACCESS_KEY' 'app.auditoria.b2.access-key')
    B2_SECRET_KEY = (Leer-Valor 'B2_SECRET_KEY' 'app.auditoria.b2.secret-key')
    B2_PATH_STYLE_ACCESS = (Leer-Valor 'B2_PATH_STYLE_ACCESS' 'app.auditoria.b2.path-style-access' 'true')
    B2_RETENTION_DAYS = (Leer-Valor 'B2_RETENTION_DAYS' 'app.auditoria.b2.retention-days' '365')
    SPRING_FLYWAY_BASELINE_ON_MIGRATE = (Leer-Valor 'SPRING_FLYWAY_BASELINE_ON_MIGRATE' 'spring.flyway.baseline-on-migrate' 'false')
    SPRING_FLYWAY_BASELINE_VERSION = (Leer-Valor 'SPRING_FLYWAY_BASELINE_VERSION' 'spring.flyway.baseline-version' '1')
    UPLOAD_DIR = '/app/uploads'
    REPORTES_DIR = '/app/reportes'
}

if ([string]::IsNullOrWhiteSpace([string]$valores['SPRING_DATASOURCE_USERNAME'])) {
    throw 'SPRING_DATASOURCE_USERNAME no puede estar vacio.'
}
try {
    $bytesAes = [Convert]::FromBase64String([string]$valores['DOCUMENTO_CLAVE_CIFRADO'])
    $bytesHmac = [Convert]::FromBase64String([string]$valores['DOCUMENTO_CLAVE_HMAC'])
} catch {
    throw 'Las claves de documento deben conservar sus valores base64 validos; no se genero ninguna clave.'
}
if ($bytesAes.Length -ne 32 -or $bytesHmac.Length -lt 32) {
    throw 'La clave AES debe tener 32 bytes y HMAC al menos 32 bytes; conserva las claves actuales.'
}
if ([Text.Encoding]::UTF8.GetByteCount([string]$valores['JWT_SECRET']) -lt 32) {
    throw 'JWT_SECRET debe tener al menos 32 bytes; conserva la clave actual.'
}

$lineas = @('# Solo para docker run --env-file. Valores literales sin comillas ni escape de $.')
foreach ($nombre in $valores.Keys) {
    $valor = [string]$valores[$nombre]
    if ($valor.IndexOfAny([char[]]@([char]0, [char]10, [char]13)) -ge 0) {
        throw "El valor de $nombre contiene NUL o salto de linea y no cabe en --env-file."
    }
    $lineas += $nombre + '=' + $valor
}
$utf8 = New-Object Text.UTF8Encoding($false)
$contenido = $utf8.GetBytes(($lineas -join "`n") + "`n")
# CreateNew garantiza que ninguna ejecucion pueda sobrescribir un archivo existente.
$flujo = [IO.File]::Open($destino, [IO.FileMode]::CreateNew, [IO.FileAccess]::Write, [IO.FileShare]::None)
try { $flujo.Write($contenido, 0, $contenido.Length) } finally { $flujo.Dispose() }
Write-Host 'Creado .env.docker con las configuraciones existentes. No se imprimieron ni generaron claves.'
Write-Host 'Solo los hosts locales de PostgreSQL y Redis se adaptaron a host.docker.internal. Se conservaron los puertos.'
