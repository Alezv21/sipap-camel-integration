$baseUrl = "http://localhost:8080/transferencias"
$today = Get-Date -Format "yyyy-MM-dd"
$yesterday = (Get-Date).AddDays(-1).ToString("yyyy-MM-dd")

$qrItau = "00020101021232400014py.gov.bcp.sip01040015021012345678905204573153036005405150005802PY5910JUAN PEREZ6008ASUNCION6304A1B2"
$qrAtlas = "00020101021232400014py.gov.bcp.sip01040007021022222222225204573153036005405200005802PY5910JUAN PEREZ6008ASUNCION6304A1B2"
$qrFamiliar = "00020101021232400014py.gov.bcp.sip01040020021098765432105204573153036005405250005802PY5910JUAN PEREZ6008ASUNCION6304A1B2"
$qrOverLimit = "00020101021232400014py.gov.bcp.sip01040015021066666666665204573153036005408100000015802PY5910JUAN PEREZ6008ASUNCION6304A1B2"
$qrUnknown = "00020101021232400014py.gov.bcp.sip01049999021055555555555204573153036005405450005802PY5910JUAN PEREZ6008ASUNCION6304A1B2"

function Send-Case($name, $id, $date, $qr) {
    Write-Host "`n====================================================" -ForegroundColor Cyan
    Write-Host $name -ForegroundColor Cyan
    Write-Host "====================================================" -ForegroundColor Cyan
    $body = @{
        id_transaccion = $id
        fecha_transaccion = $date
        qr = $qr
    } | ConvertTo-Json

    try {
        Invoke-RestMethod -Method Post -Uri $baseUrl -ContentType "application/json" -Body $body | ConvertTo-Json -Depth 10
    }
    catch {
        if ($_.ErrorDetails.Message) {
            Write-Host $_.ErrorDetails.Message -ForegroundColor Yellow
        } else {
            Write-Host $_.Exception.Message -ForegroundColor Yellow
        }
    }
    Start-Sleep -Seconds 2
}

Send-Case "CASO 1 - REST válida <= 10.000.000 enviada a Artemis" "TX-V2-001" $today $qrItau
Send-Case "CASO 2 - Monto > 10.000.000 rechazado antes del broker" "TX-V2-002" $today $qrOverLimit
Send-Case "CASO 3 - Fecha actual: consumidor procesa" "TX-V2-003" $today $qrAtlas
Send-Case "CASO 4 - Fecha anterior: consumidor rechaza" "TX-V2-004" $yesterday $qrFamiliar
Send-Case "CASO 5 - Banco mock responde OK" "TX-V2-005" $today $qrFamiliar
Send-Case "CASO 6 - Banco mock responde rechazo" "TX-REJECT-006" $today $qrItau

Write-Host "`n====================================================" -ForegroundColor Cyan
Write-Host "CASO 7 - Idempotencia: segundo envío debe ser DUPLICADA" -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan
Send-Case "Primer envío" "TX-DUP-007" $today $qrItau
Send-Case "Segundo envío con el mismo id" "TX-DUP-007" $today $qrItau

Send-Case "CASO 8 - Correlation Identifier: revisar TX-V2-008 en API, JMS, consumidor y mock" "TX-V2-008" $today $qrAtlas
Send-Case "CASO 9 - Banco desconocido" "TX-V2-009" $today $qrUnknown

Write-Host "`nFin de la demostración. Revisar también la consola de Camel para publicación, consumo, fecha, correlación y respuesta del mock." -ForegroundColor Green
