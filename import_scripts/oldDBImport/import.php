<?php
$link = mysql_connect('localhost:3307', 'skyscream', 'bountykiller');
if (!$link) {
    die('Ошибка соединения: ' . mysql_error());
}
echo 'Успешно соединились';
mysql_select_db("ai_ibold");
mysql_query("set names 'utf-8'");
mysql_query("SET character_set_results = 'utf8', character_set_client = 'utf8', character_set_connection = 'utf8', character_set_database = 'utf8', character_set_server = 'utf8'");


$result = mysql_query("
		SELECT l.id, u.nik, l.tip, t.soder, l.dt
		FROM  `talk_log` l
		JOIN chaters u ON u.id = l.user_id
		JOIN talk t ON t.id = l.talk_id
		ORDER BY user_id, dt
");


$MARISHKO_API_URL = 'http://www2:4567/';
$dialogs=[];
$prevTime=0;
$cnt=0;
$prevUser=false;
$prevText='';

while ($row = mysql_fetch_assoc($result)) {

	$currTime = $row['dt'];
	$text = $row['soder'];
	$user = $row['nik'];

	if ($prevText == $text) {
		echo $prevText.'='.$text;
		continue;
	}

	if (sizeof($dialogs) > 0 && ($currTime - $prevTime > 60 * 1000 || $prevUser !== $user)) {
		$userName = 'old'.$cnt.'_'.$prevUser;
		echo "Send dialog from :" .$prevUser .PHP_EOL;

		$myCurl = curl_init();
		foreach($dialogs as $msg) {
			curl_setopt_array($myCurl, array(
				CURLOPT_URL => $MARISHKO_API_URL.'system/teach/',
				CURLOPT_RETURNTRANSFER => true,
				CURLOPT_POST => true,
				CURLOPT_POSTFIELDS => http_build_query(array('userName'=>$userName, 'phrase'=>$msg))
			));

			$response = curl_exec($myCurl);

			echo $cnt.") Add msg '".$msg."', from ".$userName." result ". $response.PHP_EOL;
		}
		curl_close($myCurl);

		$dialogs=[];
		$cnt++;
	}


	$dialogs[] = $text;

	$prevTime = $currTime;
	$prevUser = $user;
	$prevText = $text;
}

echo 'Total ' . $cnt . ' send' . PHP_EOL;
