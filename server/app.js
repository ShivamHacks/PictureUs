var express = require('express');
var path = require('path');

var bodyParser = require('body-parser');
var morgan = require('morgan');

var app = express();

app.listen(process.env.PORT || '3000', function () {
	console.log('Server started on port: ' + this.address().port);
	// this.close(); //--> just to test forever script
});

app.use(morgan('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ limit: '5mb', extended: true }));

//app.use('/api', require('./test/api'));
app.use('/api', require('./routes/authorize'));
app.use('/api/users', require('./routes/users'));
app.use('/api/groups', require('./routes/groups'));
app.use('/api/photos', require('./routes/photos'));
app.use('/dev', require('./routes/developer')); // dev stuff: TODO: make secure

// error handlers

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  var err = new Error('Not Found');
  err.status = 404;
  next(err);
});

app.use(function(err, req, res, next) {
  res.status(err.status || 500);
  res.send('error ' + err.message);
});


module.exports = app;