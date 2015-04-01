<?php

$key = "TESTKEY";
$secret = "TESTSECRET";

// Fetch the categories ever changed since gmtModifiedStart
if(false){
	$apiUrl = "http://www.sunsky-api.com/openapi/category!getChildren.do";
	$parameters = array(
		'key'              => $key,
		'gmtModifiedStart' => '10/31/2013'
	);
	$result = OpenApiService::call($apiUrl, $secret, $parameters);
	echo $result;
}

// Fetch the products ever changed since gmtModifiedStart
if(false){
	$apiUrl = "http://www.sunsky-api.com/openapi/product!search.do";
	$parameters = array(
		'key'              => $key,
		'gmtModifiedStart' => '10/31/2012'
	);
	$result = OpenApiService::call($apiUrl, $secret, $parameters);
	echo $result;
}

// Fetch the details for the product
if(false){
	$apiUrl = "http://www.sunsky-api.com/openapi/product!detail.do";
	$parameters = array(
		'key'    => $key,
		'itemNo' => 'S-MPH-6016B'
	);
	$result = OpenApiService::call($apiUrl, $secret, $parameters);
	echo $result;
}

// Download the images
if(true){
	$apiUrl = "http://www.sunsky-api.com/openapi/product!getImages.do";
	$parameters = array(
		'key'       => $key,
		'itemNo'    => 'S-MPH-6016B',
		'size'      => '50',
		'watermark' => 'mysite.com'
	);
	$path = 'test.zip';
	OpenApiService::download($apiUrl, $secret, $parameters, $path);
}

// Fetch the countries and states
if(false){
	$apiUrl = "http://www.sunsky-api.com/openapi/order!getCountries.do";
	$parameters = array(
		'key' => $key
	);
	$result = OpenApiService::call($apiUrl, $secret, $parameters);
	echo $result;
}

// Calculate the prices and freights for the items
if(false){
	$apiUrl = "http://www.sunsky-api.com/openapi/order!getPricesAndFreights.do";
	$parameters = array(
		'key'            => $key,
		'countryId'      => '41',
		'items.1.itemNo' => 'S-IP4G-0363',
		'items.1.qty'    => '20',
		'items.2.itemNo' => 'S-MAC-0230',
		'items.2.qty'    => '5'
	);
	$result = OpenApiService::call($apiUrl, $secret, $parameters);
	echo $result;
}

/*
 *	OpenApiService
 */
class OpenApiService{

	public static function call($apiUrl, $secret, $parameters){
		$signature = OpenApiService::sign($parameters, $secret);
		$parameters['signature'] = $signature;
		$postdata = http_build_query($parameters);
		$ch = curl_init();
		curl_setopt($ch, CURLOPT_URL, $apiUrl);
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
		curl_setopt($ch, CURLOPT_POST, 1);
		curl_setopt($ch, CURLOPT_POSTFIELDS, $postdata);
		curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, 10);
		curl_setopt($ch, CURLOPT_TIMEOUT, 30);
		$output = curl_exec($ch);
		curl_close($ch);
		return $output;
	}	
	
	public static function download($apiUrl, $secret, $parameters, $path){
		$signature = OpenApiService::sign($parameters, $secret);
		$parameters['signature'] = $signature;
		$postdata = http_build_query($parameters);
		$fp = fopen($path, 'w');
		$ch = curl_init();
		curl_setopt($ch, CURLOPT_URL, $apiUrl);
		curl_setopt($ch, CURLOPT_FILE, $fp);
		curl_setopt($ch, CURLOPT_HEADER, 0);
		curl_setopt($ch, CURLOPT_POST, 1);
		curl_setopt($ch, CURLOPT_POSTFIELDS, $postdata);
		curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, 10);
		curl_setopt($ch, CURLOPT_TIMEOUT, 30);
		curl_exec($ch);
		curl_close($ch);
		fclose($fp);
	}
	
	public static function sign($parameters, $secret){
		$signature = '';
		ksort($parameters);
		foreach($parameters as $key=>$value){
			$signature .= $value;
		}
		$signature = $signature . '@' . $secret;
		$signature = md5($signature);
		return $signature;
	}
}

?>