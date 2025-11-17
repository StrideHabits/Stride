# ---
# This script requires PS-7+
# ---

param(
    [Parameter(Mandatory = $true)]
    [string]$InputDir,

    [Parameter(Mandatory = $false)]
    [string]$OutputCsv = ".\output\localization-export.csv"
)

Write-Host "Scanning for strings.xml files in '$InputDir'..."

# Find all strings.xml files recursively
$xmlFiles = Get-ChildItem -Path $InputDir -Recurse -Filter "strings.xml"

if ($xmlFiles.Count -eq 0)
{
    Write-Warning "No strings.xml files found."
    exit
}

Write-Host "Found $( $xmlFiles.Count ) strings.xml file(s)."

$allStrings = @()

foreach ($file in $xmlFiles)
{
    Write-Host "Parsing file: $( $file.FullName )"
    try
    {
        [xml]$xmlDoc = Get-Content $file.FullName
    }
    catch
    {
        Write-Warning "Failed to load XML from $( $file.FullName ): $_"
        continue
    }

    # Get all <string> elements
    $nodes = $xmlDoc.SelectNodes("//string")

    if ($nodes.Count -eq 0)
    {
        Write-Warning "No <string> nodes found in $( $file.FullName )."
        continue
    }

    foreach ($node in $nodes)
    {
        $name = $node.GetAttribute("name")
        $value = $node.InnerText
        $translatable = if ($node.Attributes["translatable"])
        {
            $node.Attributes["translatable"].Value
        }
        else
        {
            "true"
        }

        $allStrings += [PSCustomObject]@{
            Name = $name
            Value = $value
            Translatable = $translatable
        }
    }
}

if ($allStrings.Count -eq 0)
{
    Write-Warning "No strings were parsed from XML files."
    exit
}

# Export to CSV

$outputDir = Split-Path -Path $OutputCsv -Parent
if (-not (Test-Path $outputDir))
{
    New-Item -ItemType Directory -Path $outputDir -Force | Out-Null
}

Write-Host "Exporting $( $allStrings.Count ) strings to '$OutputCsv'..."
$allStrings | Export-Csv -Path $OutputCsv -NoTypeInformation -Encoding UTF8
Write-Host "Export complete: $OutputCsv"
