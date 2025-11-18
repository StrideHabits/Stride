# ---
# This script requires PS-7+
# ---
param(
    [Parameter(Mandatory = $true)]
    [string]$InputCsv,

    [Parameter(Mandatory = $false)]
    [string]$OutputDir = ".\output/import"
)

#region Internals

$ScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$OutputDir = Join-Path $ScriptRoot $OutputDir

function Load-TranslationCsv
{
    param(
        [Parameter(Mandatory = $true)]
        [string]$CsvPath
    )

    if (-not (Test-Path $CsvPath))
    {
        Write-Warning "CSV file not found: $CsvPath"
        return @()
    }

    Write-Host "Loading translations from '$CsvPath'..."
    try
    {
        return Import-Csv -Path $CsvPath -Encoding UTF8
    }
    catch
    {
        Write-Warning "Failed to read CSV: $_"
        return @()
    }
}

function Get-DetectedLanguages
{
    param(
        [Parameter(Mandatory = $true)]
        [array]$CsvRows
    )

    # Language columns are all columns except 'Name', 'Translatable', 'Value'
    $firstRow = $CsvRows | Select-Object -First 1
    return $firstRow.PSObject.Properties.Name | Where-Object{ $_ -ne 'Name' -and $_ -ne 'Translatable' -and $_ -ne 'Value' }
}

function New-StringsXmlExports
{
    param(
        [Parameter(Mandatory = $true)]
        [array]$Rows,

        [Parameter(Mandatory = $false)]
        [switch]$OmitUntranslatable
    )

    $xmlDocument = New-Object System.Xml.XmlDocument
    $xmlMetadata = $xmlDocument.CreateXmlDeclaration("1.0", "utf-8", $null)
    $xmlDocument.AppendChild($xmlMetadata) | Out-Null

    $resourcesNode = $xmlDocument.CreateElement("resources")
    $xmlDocument.AppendChild($resourcesNode) | Out-Null

    foreach ($row in $Rows)
    {
        if (-not $row.Name)
        {
            continue
        }

        # Skip untranslatable strings for translation files
        if ($OmitUntranslatable -and $row.PSObject.Properties['Translatable'] -and $row.Translatable.Trim().ToLower() -eq "false")
        {
            continue
        }

        $stringNode = $xmlDocument.CreateElement("string")
        $stringNode.SetAttribute("name", $row.Name)

        # Only set translatable="false" in default file
        if (-not $OmitUntranslatable)
        {
            $translatable = if ($row.PSObject.Properties['Translatable'])
            {
                $row.Translatable
            }
            else
            {
                "true"
            }

            if ($translatable.Trim().ToLower() -ne "true")
            {
                $stringNode.SetAttribute("translatable", $translatable)
            }
        }

        $stringNode.InnerText = $row.Value
        $resourcesNode.AppendChild($stringNode) | Out-Null
    }

    return $xmlDocument
}

function Export-StringsXml
{
    param(
        [Parameter(Mandatory = $true)]
        [string]$OutputBaseDir,

        [Parameter(Mandatory = $true)]
        [string]$FolderName,

        [Parameter(Mandatory = $true)]
        [array]$StringRows,

        [Parameter(Mandatory = $false)]
        [switch]$OmitUntranslatable
    )

    $outputPath = Join-Path -Path $OutputBaseDir -ChildPath $FolderName
    if (-not (Test-Path $outputPath))
    {
        New-Item -ItemType Directory -Path $outputPath -Force | Out-Null
    }

    $xmlDocument = New-StringsXmlExports -Rows $StringRows -OmitUntranslatable:$OmitUntranslatable
    $xmlFile = Join-Path -Path $outputPath -ChildPath "strings.xml"

    Write-Host "Writing $( $StringRows.Count ) strings to '$xmlFile'..."
    $xmlDocument.Save($xmlFile)
}

#endregion

#region Execution

$csvRows = Load-TranslationCsv -CsvPath $InputCsv
if (-not $csvRows -or $csvRows.Count -eq 0)
{
    Write-Warning "No translations found in CSV."
    exit
}

# Detect all language columns
$languageColumns = Get-DetectedLanguages -CsvRows $csvRows
Write-Host "Detected languages: $( $languageColumns -join ', ' )"

# Prepare default strings (values/)
$defaultRows = $csvRows | ForEach-Object {
    [PSCustomObject]@{
        Name = $_.Name
        Value = $_.default
        Translatable = $_.Translatable
    }
}
Export-StringsXml -OutputBaseDir $OutputDir -FolderName "values" -StringRows $defaultRows

# Prepare language-specific strings (omit untranslatable)
foreach ($language in $languageColumns)
{
    $languageRows = $csvRows | Where-Object { $_.$language } | ForEach-Object {
        [PSCustomObject]@{
            Name = $_.Name
            Value = $_.$language
            Translatable = $_.Translatable
        }
    }

    if ($languageRows.Count -gt 0)
    {
        $folderName = "values-$language"
        Export-StringsXml -OutputBaseDir $OutputDir -FolderName $folderName -StringRows $languageRows -OmitUntranslatable
    }
}

Write-Host "All translations exported successfully to '$OutputDir'."

#endregion