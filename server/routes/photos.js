var express = require('express');
var router = express.Router();
var config = require('../config');

var dbUsers = require('../helpers/dbInterface')('users');
var dbPhotos = require('../helpers/dbInterface')('photos');

// Initialized DB w/ indexes and such
dbPhotos.dropAllIndexes();
dbPhotos.createIndex({ "group": 1, "capturedAt": -1 });

var ObjectId = require('mongodb').ObjectID;

var cloudinary = require('cloudinary');
cloudinary.config(config.cloudinary);

var requester = require('request');
var _ = require('underscore');
var request = require('../helpers/request');
var encryption = require('../helpers/encryption');
var async = require('async');

router.post('/upload', function(req, res, next) {

	var r = request.new(req, res);

	var userID = r.body.userID;
	var phoneNumber = r.body.phoneNumber;
	var base = 'data:image/png;base64,';
	var dbID = new ObjectId();
	var groupID = r.body.groupID;

	cloudinary.v2.uploader.upload(base + r.body.image, { 
		public_id: groupID + '/' + dbID 
	}, function(error, result) {
		if (!error) {
			dbPhotos.put({
				_id: dbID,
				capturedBy: {
					userID: userID,
					userPhone: phoneNumber
				},
				capturedAt: r.body.capturedAt,
				group: groupID
			}, function(success, doc) {});
		}
	});

	r.success({});
});

router.get('/getAll', function(req, res, next) {

	var r = request.new(req, res);

	var userID = r.body.userid;
	var groupID = r.body.groupid;
	var token = r.body.token;

	// TODO: staggered loading (load first 15, then next 15)

	dbPhotos.getMany({ group: groupID }, function(success, docs) {
		if (success && docs.length != 0) {

			var photos = _.sortBy(_.map(docs, function(doc) { 
				return {
					url: config.cloudinaryURL + groupID + '/' + doc._id,
					capturedBy: encryption.decrypt(doc.capturedBy.userPhone),
					capturedAt: doc.capturedAt
				};
			}), function(obj) {
				return obj.capturedAt;
			});

			r.success({ photos: photos });

		} else if (docs.length == 0) {
			r.success({ photos: [] });
		} else { r.error(500, 'Error getting all photos', userID, req.url); }
	});

});

function getPhoneFromID(userID) {
	// asynchronous, so need to fix
	dbUsers.get({ _id: ObjectId(userID) }, function(success, result) {
		//console.log(result);
		if (success && !_.isEmpty(result)) return result.phoneNumber;
		else return null;
	})
}

module.exports = router;