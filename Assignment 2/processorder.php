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

		// Code block for performing file IO to update the text file
		$file = fopen("order.txt", "r+");
		if (!$file) {
			// This code block expected to run only once
			$file = fopen("order.txt", "w");
			file_put_contents("Total number of apples: ", $apples);
			file_put_contents("Total number of oranges: ", $oranges);
			file_put_contents("Total number of bananas: ", $bananas);
		}
		else {
			// Keep reading line by line until end of file
			for($i = 0; !feof($file); ++$i) {
				switch($i) {
					case 0:
						break;
					case 1:
						break;
					case 2:
						break;
				}
			}
		}
		fclose($file);
		?>
	<div class="receipt">
		<div class="summary">
			<label>Your order has been received.</label>
		</div>
		<div>
			<div class="header">
				<label>Username: </label>
			</div>
			<div class="data">
				<label> <?php echo $username; ?>
				</label>
			</div>
		</div>
		<div>
			<div class="alt header">
				<label>Number of apples: </label>
			</div>
			<div class="alt data">
				<label> <?php echo $apples; ?>
				</label>
			</div>
		</div>
		<div>
			<div class="header">
				<label>Number of bananas: </label>
			</div>
			<div class="data">
				<label> <?php echo $bananas; ?>
				</label>
			</div>
		</div>
		<div>
			<div class="alt header">
				<label>Number of oranges: </label>
			</div>
			<div class="alt data">
				<label> <?php echo $oranges; ?>
				</label>
			</div>
		</div>
		<div>
			<div class="header">
				<label>Total Cost: </label>
			</div>
			<div class="data">
				<label> <?php echo "$".$total; ?>
				</label>
			</div>
		</div>
		<div>
			<div class="alt header">
				<label>Paid Using: </label>
			</div>
			<div class="alt data">
				<label> <?php echo $payment; ?>
				</label>
			</div>
		</div>
	</div>
	<?php } catch(UnexpectedValueException $e) { ?>
	<div class="receipt">
		<div class="error">
			<label><?php echo $e->getMessage(); ?> </label>
		</div>
	</div>
	<?php } ?>
</body>
</html>
