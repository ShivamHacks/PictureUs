/*

	Here I will test all of the API functions in relation to the mobile app
	Instead of building the entire server, before finishing the mobile app,
	I will create this dummy API and send dummy data to make sure the mobile app is working.
	Then I will finish the server with the proper implementation of the API and use it for the app.

	I will not be interacting with the Database, nor will I store any data anywhere. All requests will be approved
	without validating the access token. However, when I implement the real API, all of the parameters and return
	value types will NOT change. The only thing that will change is the processing of the parameters and the values returned.

	ASSUMING NO PARAMETERS ARE NULL. ALL NULL/EMPTY VALUE CHECKING WILL BE DONE IN APP

	*/

// BEFORE DOING ANYTHING ELSE IN APP, finish this so that actual app testing can begin
// MAKE SURE that in post functions, use req.body and in get functions, use req.headers

// Dependencies
var express = require('express');
var router = express.Router();
var config = require('../app.json');
var encryption = require('../helpers/encryption');
var ObjectID = require('mongodb').ObjectID;
var jwt = require('jsonwebtoken');
var jwtSecret = config.jwtSecret;
var fs = require('fs');

router.all('/', function(req, res, next) {
	console.log("GOT SOMETHING MAIN");
	next();
})

// Users
router.post('/users/login', login);
router.post('/users/signup', signup);
router.post('/users/verify', verify);
router.post('/users/deleteAccount', deleteAccount);

// Groups and Events
router.post('/groups/createGroup', createGroup);
router.get('/groups/getAllGroups', getAllGroups);
router.get('/groups/getGroupInfo', getGroupInfo);
router.post('/groups/editGroup', editGroup);
router.post('/groups/leaveGroup', function(req, res, next) {});
router.post('/groups/deleteGroup', function(req, res, next) {});

// Photos
router.post('/photos/upload', uploadPhoto);
router.get('/photos/get', function(req, res, next) {});
router.get('/photos/getAll', getAllPhotos);
router.post('/photos/delete', function(req, res, next) {});

// Actual router functions
// All requests except for login/signup/verify will require a token which has the userID in it.
// Every request must have success

// Fake Data
var membersList = [
encryption.encrypt('0123456789'),
encryption.encrypt('9876543210'),
encryption.encrypt('5671237890')
];
var groupName = 'Three Pineapples';
var addMembersList = [];
function decryptMembers(members) {
	var mems = members;
	for (var i = 0; i < mems.length; i++)
		mems[i] = encryption.decrypt(mems[i]);
	console.log(mems);
	return mems;
}

var photoURLS = [
"https://images.unsplash.com/photo-1464740042629-b78a46b310ae",
"https://images.unsplash.com/photo-1456894332557-b03dc5cf60d5",
"https://images.unsplash.com/photo-1451479456262-b94f205059be",
"https://images.unsplash.com/photo-1467094568967-95f87ee9c873",
"https://images.unsplash.com/photo-1466721591366-2d5fba72006d",
"https://images.unsplash.com/photo-1468245856972-a0333f3f8293",
"https://images.unsplash.com/photo-1465284958051-1353268c077d",
"https://images.unsplash.com/photo-1465232377925-cce9a9d87843",
"https://images.unsplash.com/photo-1465441494912-68f5747c3fe0",
"https://images.unsplash.com/photo-1464655646192-3cb2ace7a67e",
"https://images.unsplash.com/photo-1461295025362-7547f63dbaea",
"https://images.unsplash.com/photo-1460400408855-36abd76648b9",
"https://images.unsplash.com/photo-1459197648846-52de5e4d1e9a",
"https://images.unsplash.com/reserve/Lt0DwxdqRKSQkX7439ey_Chaz_fisheye-11.jpg",
"https://images.unsplash.com/reserve/unsplash_52ce2b0530dab_1.JPG",
"https://images.unsplash.com/photo-1464822759023-fed622ff2c3b",
"https://images.unsplash.com/photo-1463392898715-3939d4ab1019",
"https://images.unsplash.com/photo-1460186136353-977e9d6085a1",
"https://images.unsplash.com/photo-1458571037713-913d8b481dc6",
"https://images.unsplash.com/photo-1452711932549-e7ea7f129399"
]

function login(req, res, next) {
	// parameters: phoneNumber, countryISO, and password
	// return userID and encrypted phone number
	var r = new Request(req, res);
	r.send(JSON.stringify({
		userID: new ObjectID(),
		phoneNumber: encryption.encrypt(r.body.phoneNumber)
	}));
}
function signup(req, res, next) {
	// parameters: phoneNumber, countryISO, and password
	// return userID and encrypted phone number
	var r = new Request(req, res);
	r.send(JSON.stringify({
		userID: new ObjectID(),
		phoneNumber: encryption.encrypt(r.body.phoneNumber)
	}));
}
function verify(req, res, next) {
	// parameters: phoneNumber, verificationCode, and userID
	// return access token
	var r = new Request(req, res);
	r.send(JSON.stringify({ 
		token: jwt.sign({ userID: userID }, jwtSecret) 
	}));
}
function deleteAccount(req, res, next) {
	// parameters: userID
	// return success or not
	var r = new Request(req, res);
	//var userID = getUID(r.body.token);
	r.send(JSON.stringify({
		success: true
	}));
}
function createGroup(req, res, next) {
	// parameters: userID, groupName, groupMembers
	// return groupID
	var r = new Request(req, res);
	console.log(r.body);
	r.send(JSON.stringify({
		success: true,
		groupID: new ObjectID(),
		members: decryptMembers(membersList),
		name: 'Three Pineapples'
	}));
}
function getGroupInfo(req, res, next) {
	// parameters: groupID
	// return group info (members, name, createdAt, createdBy, etc)
	var r = new Request(req, res);
	console.log("GOT IT");
	console.log(r.body);
	r.send(JSON.stringify({
		success: true,
		groupID: new ObjectID(),
		members: membersList,
		name: 'Three Pineapples'
	}));
	//r.send("DONE");
}
function getAllGroups(req, res, next) {
	// parameters: userID
	// return list of groupsID's and groupNames
	var r = new Request(req, res);
	r.send(JSON.stringify({
		groups: [
		{
			_id: new ObjectID(),
			groupName: 'Three Pineapples'
		},
		{
			_id: new ObjectID(),
			groupName: 'The Backstreet Boys'	
		},
		{
			_id: new ObjectID(),
			groupName: 'Family Awesome'
		}
		]
	}));
}
function getEvents(req, res, next) {
	// parameters: groupID
	// return list of eventID's and eventNames
	var r = new Request(req, res);
}
function editGroup(req, res, next) {
	// parameters: membersToAdd, membersToRemove, groupID
	// return success or not and new group info
	// nothing to be changed: body is empty
	var r = new Request(req, res);
	console.log(r);
	r.send("GOOD");
}
function leaveGroup(req, res, next) {
	// parameters: userID and groupID
	// return success or not
	var r = new Request(req, res);
}
function getAllPhotos(req, res, next) {
	var r = new Request(req, res);
	r.send(JSON.stringify({
		photoURLS: photoURLS
	}));
}
var index = 0;
function uploadPhoto(req, res, next) {
	var r = new Request(req, res);
	var buff = new Buffer(r.body.image, 'base64');
  	fs.writeFile('uploads/' + index + '.png', buff, function(err) { 
    	if (err) console.log(err);
    	index++;
  	});
  	r.send("GOOD");
}

// Helper Functions
function Request(req, res) {
	this.body = returnBody(req);
	this.send = function(result) { res.send(result); }
}
function getUID(token) {
	// no try/catch around decoding because assuming token is correct
	var decoded = jwt.verify(token, jwtSecret);
	return decoded.userID;
}
function returnBody(req) {
	if (req.method == 'POST') return req.body;
	else if (req.method == 'GET') return req.headers;
	else return req; // no change
}

module.exports = router;