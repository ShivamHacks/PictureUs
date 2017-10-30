var express = require('express');
var router = express.Router();
var config = require('../config');

// Router Index
router.post('/createGroup', createGroup);
router.get('/getAllGroups', getAllGroups);
router.get('/getGroupInfo', getGroupInfo);
router.post('/editGroup', editGroup);
router.post('/leaveGroup', leaveGroup);

// Dependencies
var dbUsers = require('../helpers/dbInterface')('users');
var dbGroups = require('../helpers/dbInterface')('groups');
var dbPhotos = require('../helpers/dbInterface')('photos');
var ObjectId = require('mongodb').ObjectID;

var cloudinary = require('cloudinary');
cloudinary.config(config.cloudinary);

var _ = require('underscore');
var encryption = require('../helpers/encryption');
var request = require('../helpers/request');

// Router Functions

function createGroup(req, res, next) {

	var r = request.new(req, res);

	var members = r.body.members;
	var userID = r.body.userID;
	var phoneNumber = r.body.phoneNumber;
	var groupName = r.body.groupName;

	getMembers(members, function(success, results) {
		if (success) {
			results.push({ userID: userID, phoneNumber: phoneNumber });
			var group = {
				name: groupName, 
				members: results, 
				createdBy: userID, 
				createdAt: Date.now()
			};
			dbGroups.put(group, function(success, doc) {
				if (success) {
					updateMemberGroups(doc);
					r.success({ 
						groupID: doc._id, 
						groupName: groupName 
					});
				} else { 
					r.error(500, 'Error Creating Group', userID, req.url); 
				}
			});
		}
	});
}

function getAllGroups(req, res, next) {
	
	var r = request.new(req, res);

	var userID = r.body.userID;

	dbUsers.get({ _id: ObjectId(userID) }, function(success, doc) {
		if (success) {
			if (_.isEmpty(doc)) { 
				r.error(500, 'User does not exist', userID, req.url);
			} else {
				getGroupNamesFromIDs(doc.groups, function(success, results) {
					if (success) r.success({ groups: results });
					else r.error(500, 'Error Getting Groups', userID, req.url);
				});
			}
		} else { 
			r.error(500, 'Error Getting Groups', userID, req.url); 
		}
	});
}

function getGroupInfo(req, res, next) {
	
	var r = request.new(req, res);

	var userID = r.body.userID;
	
	dbGroups.get({ _id: ObjectId(r.body.groupid) }, function(success, doc) {
		if (success) {
			if (_.isEmpty(doc)) { 
				r.error(404, 'Group Not Found', userID, req.url); 
			}
			else {
				var members = decryptMembers(_.pluck(doc.members, 'phoneNumber'));
				r.success({
					groupID: doc._id,
					groupName: doc.name,
					createdBy: doc.createdBy,
					createdAt: doc.createdAt,
					members: members
				});
			}
		} else { r.error(500, 'Error Getting Group Info', userID, req.url);  }
	});
}

function editGroup(req, res, next) {
	
	var r = request.new(req, res);

	var userID = r.body.userID;
	
	var params = {};
	if (typeof r.body.newName != 'undefined') {
		params.$set = { name: r.body.newName };
	}
	if (typeof r.body.newMembers != 'undefined') {
		getMembers(r.body.newMembers, function(success, results) {
			if (success) {
				params.$push = { members: { $each: results } };
				dbGroups.update({ 
					_id: ObjectId(r.body.groupID) 
				}, params, function(success, doc) {
					if (success) {
						updateMemberGroups(doc);
						r.success({});
					} else { r.error(500, 'Error Updating Group Members', userID, req.url); }
				});
			}
		});
	} else {
		if (_.isEmpty(params)) { r.success({}); } // nothing to update
		else { // only update name
			dbGroups.update({ 
				_id: ObjectId(r.body.groupID) 
			}, params, function(success, doc) {
				if (success) r.success({});
				else r.error(500, 'Error Updating Group Name', userID, req.url);
			});
		}
	}
}

function leaveGroup(req, res, next) {
	
	var r = request.new(req, res);
	
	var userID = r.body.userID;
	var groupID = r.body.groupID;

	var userParams = { $pull: { 
		groups: ObjectId(groupID) 
	} };
	var groupParams = { $pull: {
		members: { userID: userID } 
	} };

	dbUsers.update({
		_id: ObjectId(r.body.userID) 
	}, userParams, function(success, doc) {});

	dbGroups.update({
		_id: ObjectId(r.body.groupID) 
	}, groupParams, function(success, doc) {
		if (success) {
			if (!_.isEmpty(doc)) {
				if (doc.members.length == 0) deleteGroup(groupID);
			}
			r.success({});
		} else { r.error(500, 'Error leaving group', userID, req.url); }
	});
}

module.exports = router;

// Router Helper Functions

function updateMemberGroups(doc) {
	var params = { $push: { groups: doc._id } };
	for (var i = 0; i < doc.members.length; i++) {
		dbUsers.update({
			_id: ObjectId(doc.members[i].userID) 
		}, params, function(success, obj) {});
	}
}

function getMembers(members, callback) {
	// For now, ignoring phoneNumbers w/ no associated user
	var encryptedNumbers = _.map(members, function(member) { 
		return encryption.encrypt(member); 
	});
	dbUsers.getMany({ 
		phoneNumber: { $in: encryptedNumbers} 
	}, function(success, results) {
		if (success) {
			var dbNumbers = _.pluck(results, 'phoneNumber');
			var categorize = _.groupBy(encryptedNumbers, function(number) { 
				return _.contains(dbNumbers, number);  
			});
			var existingUsers = _.map(_.filter(results, function(obj) { 
				return _.contains(categorize.true, obj.phoneNumber); 
			}), function(mem) {
				return { userID: mem._id, phoneNumber: mem.phoneNumber };
			});
			callback(true, existingUsers);
		} else callback(false, null);
	});
}

function deleteGroup(groupID) {
	// This works perfectly
	dbGroups.get({ _id: ObjectId(groupID) }, function(success, group) {
		if (success) {
			if (!(_.isEmpty(group))) {

				dbGroups.remove({
					_id: ObjectId(groupID) 
				}, function(success) {});

				dbPhotos.remove({ 
					group: groupID 
				}, function(success) {});

				cloudinary.api.delete_resources_by_prefix(groupID, function(result) {});

			}
		}
	});
}
function decryptMembers(members) {
	for (var i = 0; i < members.length; i++)
		members[i] = encryption.decrypt(members[i]);
	return members;
}

function getGroupNamesFromIDs(ids, callback) {
	dbGroups.getMany({ 
		_id: { $in: ids } 
	}, function(success, results) {
		if (success) {
			var groups = _.map(results, function(doc) {
				return { 
					groupID: doc._id,
					groupName: doc.name
				};
			});
			callback(true, groups);
		} else { callback(false, null); }
	});
}