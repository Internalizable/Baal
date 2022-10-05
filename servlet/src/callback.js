const http = require("http");
const url = require('url');
const querystring = require('querystring');
const axios = require('axios');
const redis = require("redis");
const express = require('express');
const config = require('config');
const crypto = require('crypto');
const app = new express();

console.log("go stupid")

const host = 'localhost';
const port = 8000;

var publisher = redis.createClient();

const redirectLink = config.get('redirectLink');
const callbackURL = config.get('callbackURL');
const consumerKey = config.get('consumerKey');
const consumerKeySecret = config.get('consumerKeySecret');

console.log("started")

app.get('/api/link/confirm', function (req, res) {
    const queryObject = url.parse(req.url,true).query;
    if(queryObject.oauth_token) {
        if(queryObject.oauth_verifier) {

            axios.post('https://api.twitter.com/oauth/access_token?oauth_token=' + queryObject.oauth_token + '&oauth_verifier=' + queryObject.oauth_verifier)
                .then(response => {
                    let returnData = querystring.parse(response.data);

                    publisher.publish("twitter-oauth-callback", JSON.stringify(returnData));

                    console.log("redirecting to main page");

                    res.redirect('http://localhost/');
                })
                .catch(error => {
                    if(error.response.status == '401') {
                        res.redirect(redirectLink);
                    }
                });
        }
    }

});

app.get('/api/link/start', function (req, res) {
    if(consumerKey) {
        if(callbackURL) {
            console.log("Consumer key and callback URL recieved, posting request");
            console.log(consumerKey);
            console.log(callbackURL);

            const token = crypto.randomBytes(Math.ceil(42 * 3 / 4))
                .toString('base64')
                .slice(0, 42)
                .replace(/\+/g, '0')
                .replace(/\//g, '0');

            let text = "oauth_callback=" + callbackURL + "&oauth_consumer_key=" + consumerKey + "&oauth_nonce=" + token + "&oauth_signature_method=HMAC-SHA1&oauth_timestamp=" + Math.floor((new Date()).getTime() / 1000) + "&oauth_version=1.0";
            let key = consumerKeySecret + "&";

            let finalizedText = "POST&" + encodeURIComponent("https://api.twitter.com/oauth/request_token") + "&" + encodeURIComponent(text);

            let signature = crypto.createHmac('sha1', key)
                .update(finalizedText)
                .digest('base64');

            axios.post('https://api.twitter.com/oauth/request_token', {}, {
                headers: {
                    'Authorization': `OAuth oauth_nonce="` + token + `", oauth_callback="` + callbackURL + `", oauth_signature_method="HMAC-SHA1", oauth_timestamp="` + Math.floor((new Date()).getTime() / 1000) + `", oauth_consumer_key="` + consumerKey + `", oauth_signature="` + encodeURIComponent(signature) + `", oauth_version="1.0"`
                }
            })
                .then(response => {
                    const returnData = querystring.parse(response.data);

                    console.log("return data");
                    if(returnData.oauth_callback_confirmed) {
                        console.log("confirmed");
                        res.redirect('https://api.twitter.com/oauth/authorize?oauth_token=' + returnData.oauth_token);
                    }

                })
                .catch(error => {
                    console.log(error.response);
                    if(error.response.status == '401') {
                        console.log(error.response);
                    }
                });
        }

    }

});

app.listen(8000);