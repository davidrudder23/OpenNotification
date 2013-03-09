#!/bin/sh

CAT <<"EOF"
<?xml version='1.0' encoding='utf-8' ?><SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"><SOAP-ENV:Body><ns1:sendNotification xmlns:ns1="http://localhost:8088/notification/SendSOAPNotification.jws"><memberName xsi:type="xsd:string">0000001</memberName><summary xsi:type="xsd:string">test</summary><message xsi:type="xsd:string">testing soap via proxy</message></ns1:sendNotification></SOAP-ENV:Body></SOAP-ENV:Envelope>
