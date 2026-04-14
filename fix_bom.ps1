$p = 'C:\git\finalProject_3\finalProject_3\src\main\java\com\spring\app\admin\service\AdminService_imple.java'
$b = [System.IO.File]::ReadAllBytes($p)
if ($b[0] -eq 0xEF -and $b[1] -eq 0xBB -and $b[2] -eq 0xBF) {
    $nb = $b[3..($b.Length-1)]
    [System.IO.File]::WriteAllBytes($p, $nb)
    Write-Output 'BOM 제거 완료'
} else {
    Write-Output 'BOM 없음'
}
