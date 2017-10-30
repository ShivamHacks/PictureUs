var express = require('express');
var router = express.Router();
var config = require('../config');

var countries = require('country-data').countries;

var unverifiedUsersDB = require('../helpers/dbInterface')('unverifiedUsers');
unverifiedUsersDB.remove({}, function(success) {});

var dbUsers = require('../helpers/dbInterface')('users');
var dbGroups = require('../helpers/dbInterface')('groups');
var dbPhotos = require('../helpers/dbInterface')('photos');

// Initialized DB w/ indexes and such
dbUsers.dropAllIndexes();
dbUsers.createIndex({ "phoneNumber": 1 });

var ObjectId = require('mongodb').ObjectID;

var verificationGen = require('randomstring');
var encryption = require('../helpers/encryption');
var jwt = require('jsonwebtoken');

var jwtSecret = config.jwtSecret;
var nativeNumber = config.nativeNumber;

var _ = require('underscore');
var request = require('../helpers/request');

var nodemailer = require('nodemailer');
var transporter = nodemailer.createTransport('smtps://' 
	+ config.sms.name + '%40gmail.com:' 
	+ config.sms.pass + '@smtp.gmail.com');
var countries = require('../countries.json');

// for login, don't need text verification

router.post('/login', function(req, res, next) {

	var r = request.new(req, res);
	
	var phoneNumber = r.body.phoneNumber;
	var password = r.body.password;

	dbUsers.get({ 
		phoneNumber: encryption.encrypt(phoneNumber)
	}, function(err, doc) {
		if (!(_.isEmpty(doc))) {
			if (doc.password == encryption.encrypt(password)) {
				// user has correct credentials

				r.success({
					token: jwt.sign({ 
						userID: doc._id, 
						phoneNumber: doc.phoneNumber 
					}, jwtSecret) 
				});

			} else { r.error(400, 'Incorrect password', null, req.url); }
		} else { r.error(400, 'Account does not exist', null, req.url); }
	});

});

router.post('/signup', function(req, res, next) {

	var r = request.new(req, res);

	var phoneNumber = r.body.phoneNumber;
	var password = r.body.password;
	var country = countries[r.body.country.trim()];

	unverifiedUsersDB.put({
		phoneNumber: encryption.encrypt(phoneNumber),
		password: encryption.encrypt(password),
		verificationCode: verificationGen.generate({ length: 6, charset: 'numeric' }),
	}, function(success, doc) {
		if (success) {
			r.success({ userID: doc._id });
			sendVerificationText(phoneNumber, doc.verificationCode, country);
		} else { r.error(500, 'Something went wrong', null, req.url); }
	});

});

router.post('/verify', function(req, res, next) {
	// only for signup

	var r = request.new(req, res);

	var userID = r.body.userID;
	var verificationCode = r.body.verificationCode;
	var intent = r.body.intent;
	var phoneNumber = r.body.phoneNumber;

	unverifiedUsersDB.get({ _id: ObjectId(userID) }, function (success, result) {

		if (success && !(_.isEmpty(result))) {
			if (verificationCode == result.verificationCode) {
				delete result.verificationCode;
				delete result._id;

					dbUsers.remove({ phoneNumber: result.phoneNumber }, function(success) {
						if (success) {
							result.groups = [];
							dbUsers.put(result, function (success, doc) {
								if (success) {

									r.success({
										token: jwt.sign({ 
											userID: doc._id, 
											phoneNumber: doc.phoneNumber 
										}, jwtSecret) 
									});

								} else { r.error(500, 'Error verifying account', null, req.url); }
							});
						} else { r.error(500, 'Error verifying account', null, req.url); }
					});

				unverifiedUsersDB.remove({ _id: ObjectId(userID) }, function(success) {});

			} else { r.error(400, 'Incorrect verification code', null, req.url); }
		} else { r.error(500, 'Error verifying account', null, req.url); }

	});

});

router.post('/deleteAccount', function(req, res, next) {

	var r = request.new(req, res);

	var userID = r.body.userID;

	r.success({});

	dbUsers.remove({ _id: ObjectId(userID) }, function(success) {});
	dbGroups.getMany({ members: { $elemMatch: { userID: userID } } }, function(success, docs) {
		if (success) {
			for (var i = 0; i < docs.length; i++) {
				dbGroups.update({ _id: ObjectId(docs[i]._id) }, {
					$pull: { members: { userID: userID } }
				}, function(success, doc) {});
				// TODO: handle groups w/ no members
			}
		}
	});

});

function sendVerificationText(num, code, country) {
	var mailOps = {
		to: '',
		text: 'PictureUs verification Code: ' + code
	};
	var carriers = require('../carriers.json')[country];
	for (var j = 0; j < carriers.length-1; j++) {
		mailOps.to += num + carriers[j] + ',';
	}
	mailOps.to += carriers[carriers.length - 1];
	console.log(mailOps);
	transporter.sendMail(mailOps, function(error, info){
    	if(error) { return console.log(error); }
    	console.log('Message sent: ' + info.response);
	});
}

module.exports = router;
