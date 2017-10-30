// these are the protected routes where I can see all of the errors
// and the status of the server so I can ping db and such

var express = require('express');
var router = express.Router();
var config = require('../config');

var MongoClient = require('mongodb').MongoClient;
var url = config.mongoURL;

var dbErrors = require('../helpers/dbInterface')('errors');
var dbUnverified = require('../helpers/dbInterface')('unverifiedUsers');
var dbUsers = require('../helpers/dbInterface')('users');
var dbGroups = require('../helpers/dbInterface')('groups');
var dbPhotos = require('../helpers/dbInterface')('photos');

var async = require('async');
var request = require('../helpers/request');

// TODO: clear all DB's
// also, change verification method lol

// Deploy on heroku 24 hrs

function canAccess(req) {
	var r = request.new(req);
	if (r.body.key1 == config.adminKey1 
		&& r.body.key2 == config.adminKey2) 
		return true;
	else return false;
}

router.get('/dbStatus', function(req, res, next) {
	if (canAccess(req)) {
		MongoClient.connect(url, function(err, db) {
			if (err) {
				res.json({
					success: false,
					error: err
				});
			}
			else {
				res.json({
					success: true,
					message: 'MongoDB is online!'
				});
			}
			db.close();
		});
	} else { res.send('Not Authorized'); }
});

router.get('/dbAll', function(req, res, next) {
	if (canAccess(req)) {
		var body = {};
		async.waterfall([
			function(callback) {
				dbUsers.getMany({}, function(success, docs) { 
					if (success) {
						body.users = docs;
						callback(null, body); 
					} else {
						callback(docs); // docs = err
					}
				});
			},
			function(body, callback) {
				dbUnverified.getMany({}, function(success, docs) { 
					if (success) {
						body.unverified = docs;
						callback(null, body); 
					} else {
						callback(docs); // docs = err
					}
				});
			},
			function(body, callback) {
				dbGroups.getMany({}, function(success, docs) { 
					if (success) {
						body.groups = docs;
						callback(null, body); 
					} else {
						callback(docs); // docs = err
					}
				});
			},
			function(body, callback) {
				dbPhotos.getMany({}, function(success, docs) { 
					if (success) {
						body.photos = docs;
						callback(null, body); 
					} else {
						callback(docs); // docs = err
					}
				});
			},
			function(body, callback) {
				dbErrors.getMany({}, function(success, docs) { 
					if (success) {
						body.errors = docs;
						callback(null, body); 
					} else {
						callback(docs); // docs = err
					}
				});
			}
			], function(err, result) {
				if (err) res.json({ success: false, err: err });
				else res.json(result);
			});
	} else { res.send('Not Authorized'); }
});

router.get('/dbStats', function(req, res, next) {
	/*MongoClient.connect(url, function(err, db) {
		if (err) { res.json({ success: false, error: err }); }
		else {
			var stats = {};
			db.listCollections().toArray(function(err, collections) {
				if (!err) {
					for (var i = 0; i < collections.length; i++) {
						console.log(collections[i]);
						stats[collections[i].name] = db.collection(collections[i].name).stats();
						console.log(stats);
					}

							res.json(stats);
				}
			});
		}
		db.close();
	});*/
	// TODO:
	res.send('lol');
});

router.post('/dbClear', function(req, res, next) {
	if (canAccess(req)) {
		dbUsers.remove({}, function(success) {});
		dbUnverified.remove({}, function(success) {});
		dbGroups.remove({}, function(success) {});
		dbPhotos.remove({}, function(success) {});
		res.send('Deleting all DB rows');
	} else { res.send('Not Authorized'); }
});

router.post('/dbClearOne', function(req, res, next) {
	if (canAccess(req)) {
		if (req.body.remove == 'users') dbUsers.remove({}, function(success) {});
		else if (req.body.remove == 'groups') dbGroups.remove({}, function(success) {});
		else if (req.body.remove == 'photos') dbPhotos.remove({}, function(success) {});
		res.send('Deleting all DB rows in: ' + req.body.remove);
	} else { res.send('Not Authorized'); }
});

router.get('/dbErrors', function(req, res, next) {
	if (canAccess(req)) {
		var skip = req.body.skip == undefined ? 0 : parseInt(req.body.skip);
		MongoClient.connect(url, function(err, db) {
			if (err) { res.json({ success: false, error: err }); }
			else {
				db.collection('errors').find({}).limit(10).skip(skip).toArray(function(err, docs) {
					if (err) {
						res.json({
							success: false,
							error: err
						});
					}
					else {
						res.json(docs);
					}
					db.close();
				});
			}
		});
	} else { res.send('Not Authorized'); }
});

router.post('/dbClearErrors', function(req, res, next) {
	if (canAccess(req)) {
		MongoClient.connect(url, function(err, db) {
			if (err) {
				res.json({ success: false, error: err });
			} else {
				db.collection('errors').remove({});
				res.json({
					success: true,
					message: 'Cleared Errors'
				});
				db.close();
			}
		});
	} else { res.send('Not Authorized'); }
});

module.exports = router;