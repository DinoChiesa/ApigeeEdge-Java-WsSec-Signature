#! /usr/local/bin/node
/*jslint node:true */
// provisionKvm.js
// ------------------------------------------------------------------
// load a JKS into Apigee Edge KVM
//
// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// last saved: <2019-August-01 13:55:35>

var fs = require('fs'),
    edgejs = require('apigee-edge-js'),
    common = edgejs.utility,
    apigeeEdge = edgejs.edge,
    sprintf = require('sprintf-js').sprintf,
    readlineSync = require('readline-sync'),
    Getopt = require('node-getopt'),
    version = '20180222-0830',
    defaults = { mapname : 'jkssecrets' },
    getopt = new Getopt(common.commonOptions.concat([
      ['e' , 'env=ARG', 'required. the Edge environment for which to store the KVM data'],
      ['m' , 'mapname=ARG', 'optional. name of the KVM in Edge for keys. Will be created if nec. Default: ' + defaults.mapname],
      ['X' , 'notencrypted', 'optional. If creating a KVM, set it as not encrypted. Default: encrypted.'],
      ['f' , 'force', 'optional. will delete the map if it exists, without prompting.'],
      ['F' , 'jksfile=ARG', 'required. name of the file containing the JKS.'],
      ['A' , 'jksalias=ARG', 'required. alias of the key in the JKS.'],
      ['P' , 'jkspassword=ARG', 'required. password for the key in the JKS.']

    ])).bindHelp();

// ========================================================================================

console.log(
  'Apigee Edge KVM-provisioning tool, version: ' + version + '\n' +
    'Node.js ' + process.version + '\n');

common.logWrite('start');

function handleError(e, result) {
  if (e) {
    common.logWrite('error: ' + JSON.stringify(e, null, 2));
    common.logWrite(JSON.stringify(result, null, 2));
    //console.log(e.stack);
    process.exit(1);
  }
}

function base64_encode(filename) {
    var bitmap = fs.readFileSync(filename);
    return new Buffer(bitmap).toString('base64');
}
function loadKvm(org, isCps, cb) {
  var re = new RegExp('(?:\r\n|\r|\n)', 'g');
  var filecontent = base64_encode(opt.options.jksfile).replace(re,'\\n');
  var options = {
        env: opt.options.env,
        kvm: opt.options.mapname
      };
  if (isCps) {
    options.key = 'jks-base64';
    options.value = filecontent;
    common.logWrite('storing the keyvaluemap data');
    org.kvms.put(options, function(e, result) {
      options.key = 'jks-alias';
      options.value = opt.options.jksalias;
      org.kvms.put(options, function(e, result) {
        options.key = 'jks-password';
        options.value = opt.options.jkspassword;
        org.kvms.put(options, cb);
      });
    });
  }
  else {
    // not CPS - can update in a block
    options.entries = {
      'jks-base64' : filecontent,
      'jks-alias' : opt.options.jksalias,
      'jks-password' : opt.options.jkspassword
    };
    common.logWrite('storing the keyvaluemap data');
    org.kvms.put(options, cb);
  }
}


function keysLoadedCb(e, result) {
  handleError(e, result);
  common.logWrite('ok. the data was loaded successfully.');
}

function deleteMap(org, cb) {
  var options = {
    env: opt.options.env,
    name: opt.options.mapname
  };
  org.kvms.del(options, cb);
}

function createMap(org, cb) {
  common.logWrite('creating the map %s', opt.options.mapname);
  var options = {
    env: opt.options.env,
    name: opt.options.mapname,
    encrypted: (!opt.options.notencrypted)
  };
  org.kvms.create(options, cb);
}

// ========================================================================================

// process.argv array starts with 'node' and 'scriptname.js'
var opt = getopt.parse(process.argv.slice(2));

if ( !opt.options.env ) {
  console.log('You must specify an environment');
  getopt.showHelp();
  process.exit(1);
}

if ( !opt.options.mapname ) {
  common.logWrite(sprintf('defaulting to %s for KVM mapname', defaults.mapname));
  opt.options.mapname = defaults.mapname;
}

common.verifyCommonRequiredParameters(opt.options, getopt);

if ( !opt.options.jksalias || !opt.options.jkspassword || !opt.options.jksfile) {
  common.logWrite('Specify --jksfile --jksalias --jkspassword');
  getopt.showHelp();
  process.exit(1);
}
var options = {
      mgmtServer: opt.options.mgmtserver,
      org : opt.options.org,
      user: opt.options.username,
      password: opt.options.password,
      no_token: opt.options.notoken,
      verbosity: opt.options.verbose || 0
    };

apigeeEdge.connect(options, function(e, org) {
  handleError(e, org);
  common.logWrite('connected');
  org.getProperties(function(e, orgProperties) {
    handleError(e, orgProperties);
    console.log('props: ' + JSON.stringify(orgProperties));
    org.kvms.get({ env: opt.options.env }, function(e, kvmresult) {
      handleError(e, kvmresult);
      if (orgProperties['features.isCpsEnabled']) {
        if (kvmresult.indexOf(opt.options.mapname) > -1) {
          loadKvm(org, true, keysLoadedCb);
        }
        else {
          createMap(org, function(e, result) {
            handleError(e, result);
            loadKvm(org, true, keysLoadedCb);
          });
        }
      }
      else {
        if (kvmresult.indexOf(opt.options.mapname) > -1) {
          if ( ! opt.options.force) {
            var ok = readlineSync.question(sprintf("\n**\nPossible data loss.\nThe map '%s' will BE DELETED. Ctrl-C to halt, ENTER To continue.", opt.options.mapname));
            console.log('\n');
          }
          deleteMap(org, function(e, result){
            handleError(e, result);
            createMap(org, function(e, result) {
              handleError(e, result);
              loadKvm(org, false, keysLoadedCb);
            });
          });
        }
        else {
          createMap(org, function(e, result) {
            handleError(e, result);
            loadKvm(org, false, keysLoadedCb);
          });
        }
      }
    });
  });

});
