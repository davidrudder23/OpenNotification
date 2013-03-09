#!/usr/bin/perl -w
use SOAP::Lite;
use Getopt::Long;

my $subject = '';
my $message = '';
my $recipient = '';

my $uri = "http://admin:password\@localhost:8080/notification/SendSOAPNotification.jws";

my $nagiosurl = 'http://localhost/nagios/cgi-bin/cmd.cgi';
my $isservice = '';
my $hostname = '';
my $objectname = '';

GetOptions ('subject=s' => \$subject, 'message=s' => \$message, 'recipient=s' => \$recipient, 'uri=s' => \$uri, 'isservice=s' => \$isservice, 'hostname=s' => \$hostname, 'objectname=s' => \$objectname);

if ($isservice =~ /^[yYtT]/) {
	$issservice = "true";	
} else {
	$issservice = "false";
}
SOAP::Lite
  -> uri($uri)
  -> proxy($uri)
  -> sendNagiosNotification(SOAP::Data->type(string => $recipient), 
  SOAP::Data->type(string => $subject), 
  SOAP::Data->type(string => $message),
  SOAP::Data->type(string => $nagiosurl),
  SOAP::Data->type(boolean => $isservice),
  SOAP::Data->type(string => $hostname),
  SOAP::Data->type(string => $objectname)
);
