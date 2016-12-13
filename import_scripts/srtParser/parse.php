<?php 

require __DIR__ . '/vendor/autoload.php';


$MARISHKO_API_URL = 'http://www2:4567/';


try{

	// westworld
	$files = scandir('./srt/');

	$dialogs=[];
	$dialogId=0;
	$prevTime=0;
	$cnt=0;
	mb_internal_encoding("UTF-8");

	foreach ($files as $fn) {
		$fn = './srt/'.$fn;

		if (!file_exists($fn) || !is_file($fn) || strpos($fn, '.srt') == false)
			continue;

	    $file = new \SrtParser\srtFile($fn);

		$dialogId++;
		foreach ($file->getSubs() as $sub) {
			$texts = split("\n", $sub->getText());

			foreach($texts as $text) {

				$currTime = $sub->getStart();
				$text = $sub->getText();

				if ($currTime - $prevTime > 60 * 1000) {
					$dialogId++;
				}

				if (strlen($text) > 100 || mb_strtoupper($text) == $text) {
					$dialogId++;
					continue;
				}

				$dialogs[$dialogId][] = $text;
				$cnt++;

				$prevTime = $currTime;
			}

		}

	}

	echo "Total dialogs: " . count($dialogs).PHP_EOL;
	echo "Total phrases: " . $cnt.PHP_EOL;

	foreach ($dialogs as $id=>$dialog) {
		if (sizeof($dialog) < 2) continue;

		foreach($dialog as $phrase) {

			$myCurl = curl_init();

			$userName = 'WestWorld' . $id;

			curl_setopt_array($myCurl, array(
				CURLOPT_URL => $MARISHKO_API_URL.'system/teach/',
				CURLOPT_RETURNTRANSFER => true,
				CURLOPT_POST => true,
				CURLOPT_POSTFIELDS => http_build_query(array('userName'=>$userName, 'phrase'=>$phrase))
			));

			$response = curl_exec($myCurl);
			curl_close($myCurl);

			echo "Add msg '".$phrase."', from ".$userName." result ". $response.PHP_EOL;
		}
		//echo 'Dialog: '.$id.', cnt:'.sizeof($dialog).PHP_EOL;
	}
	//var_dump($dialogs);
}
catch(Exception $e){
    echo 'Error: '.$e->getMessage()."\n";
}