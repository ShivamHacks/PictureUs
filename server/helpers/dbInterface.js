var MongoClient = require('mongodb').MongoClient;
var config = require('../config');
var url = config.mongoURL;

module.exports = function(collection) {
	var exports = {};

	exports.put = function(obj, callback) {
		MongoClient.connect(url, function(err, db) {
			if (err) { callback(false, err); }
			else {
				db.collection(collection).insert(obj, function(err, result) {
					if (err) callback(false, err);
					else callback(true, obj);
					db.close();
				});
			}
		});
	};

	exports.get = function(obj, callback) {
		MongoClient.connect(url, function(err, db) {
			if (err) { callback(false, err); }
			else {
				db.collection(collection).findOne(obj, function(err, document) {
					if (err) callback(false, err);
					else callback(true, document);
					db.close();
				});
			}
		});
	};

	exports.update = function(query, params, callback) {
		MongoClient.connect(url, function(err, db) {
			if (err) { callback(false, err); }
			else {
				db.collection(collection).findAndModify(query, [], params, { new: true }, function(err, doc) {
					if (err) callback(false, err);
					else callback(true, doc.value);
					db.close();
				});
			}
		});
	};

	/*exports.updateMany = function(query, params, callback) {
		MongoClient.connect(url, function(err, db) {
			if (err) { callback(false, err); }
			else {
				db.collection(collection).update(query, params, function(err, result) {
					if (err) callback(false);
					else callback(true);
				})
			}
		});
	};*/

	exports.remove = function(obj, callback) {
		MongoClient.connect(url, function(err, db) {
			if (err) { callback(false, err); }
			else {
				db.collection(collection).remove(obj, function(err, results) {
					if (err) callback(false);
					else callback(true);
					db.close();
				});
			}
		});
	}

	exports.getMany = function(params, callback) {
		MongoClient.connect(url, function(err, db) {
			if (err) { callback(false, err); }
			else {
				db.collection(collection).find(params).toArray(function(err, docs) {
					if (err) callback(false, err);
					else callback(true, docs);
					db.close();
				});
			}
		});
	};

	// Indexes

	exports.createIndex = function(params) {
		MongoClient.connect(url, function(err, db) {
			if (err) { ; }
			else {
				db.collection(collection).createIndex(params);
			}
			db.close();
		});
	}

	exports.listAllIndexes = function() {
		MongoClient.connect(url, function(err, db) {
			if (err) { return []; }
			else {
				return null //db.people.getIndexes();
			}
			db.close();
		});
	}

	exports.dropAllIndexes = function() {
		MongoClient.connect(url, function(err, db) {
			if (err) { ; }
			else {
				db.collection(collection).dropIndexes();
			}
			db.close();
		});
	}

	return exports;
}