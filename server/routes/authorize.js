var express = require('express');
var router = express.Router();
var config = require('../config');
// var errors = require('../errors'); // json document containing all errors

var jwt = require('jsonwebtoken');
var jwtSecret = config.jwtSecret;

// TODO: what if valid user, but different token than user in app

router.use('/', function(req, res, next) {
	// authorize here 
	if (req.url == '/users/login' || req.url == '/users/signup' || req.url == '/users/verify') { next(); } 
	else {
		// body -> POST, headers -> GET, params & query -> Misc.
		var token = req.body.token || req.query.token || req.params.token || req.headers.token;
		if (token) {
			try {
				var decoded = jwt.verify(token, jwtSecret);
				// if decoded, that means that user is authorized
				if (req.method == 'POST') {
					req.body.userID = decoded.userID;
					req.body.phoneNumber = decoded.phoneNumber;
				} else if (req.method == 'GET') {
					req.headers.userID = decoded.userID;
					req.headers.phoneNumber = decoded.phoneNumber;
				}
				next();
			} catch(err) {
				res.send(JSON.stringify({
					success: false,
					message: 'Error verifying token'
				}));
			}
		} else {
			res.send(JSON.stringify({
				success: false,
				message: 'Not authorized'
			}));
		}
	}
});

module.exports = router;
