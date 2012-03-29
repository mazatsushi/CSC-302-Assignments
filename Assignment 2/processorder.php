<!DOCTYPE HTML>
<html>
<head>
<meta charset="UTF-8">
<link rel="stylesheet" type="text/css" href="styles.css" />
<title>Order Processed</title>
</head>
<body>
	<?php
	$username = $_REQUEST["username"];
	$apples = $_REQUEST["apples"];
	$bananas = $_REQUEST["bananas"];
	$oranges = $_REQUEST["oranges"];
	$payment = $_REQUEST["payment"];
	$numericRegex = "/\d+/";
	$whiteSpaceRegex = "/\s+/";
	try {
		// Code block for checking for invalid input values
		if (preg_match($whiteSpaceRegex, $username) || strlen($username) < 1) {
			throw new UnexpectedValueException("Invalid user name.");
		}
		if (!preg_match($numericRegex, $apples) || $apples < 0) {
			throw new UnexpectedValueException("Invalid number of apples.");
		}
		if (!preg_match($numericRegex, $bananas) || $bananas < 0) {
			throw new UnexpectedValueException("Invalid number of bananas.");
		}
		if (!preg_match($numericRegex, $oranges) || $oranges < 0) {
			throw new UnexpectedValueException("Invalid number of oranges.");
		}
		if (preg_match($whiteSpaceRegex, $payment) || strlen($payment) < 1) {
			throw new UnexpectedValueException("Invalid payment mode.");
		}

		$total = (0.69 * $apples) + (0.59 * $bananas) + (0.39 * $oranges);
		echo '<div class="receipt">
		<div class="summary">
		<label>Your order has been received.</label>
		</div>
		<div class="header">
		<label>Username: </label>
		</div>
		<div class="data">
		<label>';
		echo $username;
		echo '</label>
		</div>
		<div class="alt header">
		<label>Number of apples: </label>
		</div>
		<div class="alt data">
		<label>';
		echo $apples;
		echo '</label>
		</div>
		<div class="header">
		<label>Number of bananas: </label>
		</div>
		<div class="data">
		<label>';
		echo $bananas;
		echo '</label>
		</div>
		<div class="alt header">
		<label>Number of oranges: </label>
		</div>
		<div class="alt data">
		<label>';
		echo $oranges;
		echo '</label>
		</div>
		<div class="header">
		<label>Total Cost: </label></div>
		<div class="data">
		<label>';
		echo "$".$total;
		echo '</label>
		</div>
		<div class="alt header">
		<label>Paid Using: </label>
		</div>
		<div class="alt data">
		<label>';
		echo $payment;
		echo '</label>
		</div>
		</div>';
	}
	catch(UnexpectedValueException $e) {
		echo '<div class="receipt">
		<div class="error">
		<label>';
		echo $e->getMessage();
		echo '</label>
		</div>';
	}
	?>
</body>
</html>
