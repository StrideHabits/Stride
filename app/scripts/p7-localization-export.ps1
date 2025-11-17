# ---
# This script requires PS-7+
# ---
param(
    [Parameter(Mandatory = $true)]
    [string]$InputDir,

    [Parameter(Mandatory = $false)]
    [string]$OutputCsv = ".\output\localization-export.csv"
)

#region Internals

function Fetch-StringsXmlFile
{
    param(
        [string]$Directory
    )

    Write-Host "Scanning for strings.xml files in '$Directory'..."
    $files = Get-ChildItem -Path $Directory -Recurse -Filter "strings.xml" -ErrorAction SilentlyContinue

    if (-not $files)
    {
        Write-Warning "No strings.xml files found in '$Directory'"
        return @()
    }

    Write-Host "Found $( $files.Count ) strings.xml file(s)."
    return $files
}

function Parse-StringsXmlFile
{
    param(
        [string]$FilePath
    )

    Write-Host "Parsing file: $FilePath"
    try
    {
        [xml]$xmlDocument = Get-Content -Path $FilePath -ErrorAction Stop
    }
    catch
    {
        Write-Warning "Failed to load XML from $FilePath : $_"
        return @()
    }

    $xmlNodes = $xmlDocument.SelectNodes("//string")
    if (-not $xmlNodes -or $xmlNodes.Count -eq 0)
    {
        Write-Warning "No <string> nodes found in $FilePath."
        return @()
    }

    return $xmlNodes | ForEach-Object {
        [PSCustomObject]@{
            Name = $_.GetAttribute("name")
            Value = $_.InnerText
            Translatable = if ($_.Attributes["translatable"])
            {
                $_.Attributes["translatable"].Value
            }
            else
            {
                "true"
            }
        }
    }
}


function Build-ExportCsv
{
    param(
        [Parameter(Mandatory = $true)]
        [array]$StringNodes,

        [Parameter(Mandatory = $true)]
        [string]$CsvDirectory
    )

    $outputDir = Split-Path -Path $CsvDirectory -Parent
    if (-not (Test-Path $outputDir))
    {
        New-Item -ItemType Directory -Path $outputDir -Force | Out-Null
    }

    Write-Host "Exporting $( $StringNodes.Count ) strings to '$CsvDirectory'..."
    $StringNodes | Export-Csv -Path $CsvDirectory -NoTypeInformation -Encoding UTF8
    Write-Host "Export complete: $CsvDirectory"
}

#endregion
#region Execution

$strings = @()
$targets = Fetch-StringsXmlFile -Directory $InputDir
foreach ($file in $targets)
{
    $strings += Parse-StringsXmlFile -FilePath $file.FullName
}

if ($strings.Count -eq 0)
{
    Write-Warning "No strings were parsed from XML files."
    exit
}

Build-ExportCsv -StringNodes $strings -CsvDirectory $OutputCsv

#endregion
