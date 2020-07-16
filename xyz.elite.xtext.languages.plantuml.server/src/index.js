var express = require('express');
var plantuml = require('node-plantuml');
 
var app = express();
 
plantuml.useNailgun(); // Activate the usage of Nailgun
 
app.get('/png/:uml', function(req, res) {
  res.set('Content-Type', 'image/png');
 
  var decode = plantuml.decode(req.params.uml);
  var gen = plantuml.generate({format: 'png'});
 
  decode.out.pipe(gen.in);
  gen.out.pipe(res);
});
 
app.get('/svg/:uml', function(req, res) {
  res.set('Content-Type', 'image/svg+xml');
 
  var decode = plantuml.decode(req.params.uml);
  var gen = plantuml.generate({format: 'svg'});
 
  decode.out.pipe(gen.in);
  gen.out.pipe(res);
});
 
app.listen(8081);
