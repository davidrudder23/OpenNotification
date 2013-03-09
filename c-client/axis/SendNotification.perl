#!/usr/bin/perl -w
use SOAP::Lite;
use Getopt::Long;

my $subject = '';
my $message = '';
my $recipient = '';

my $uri = "http://admin:password\@localhost:8080/notification/SendSOAPNotification.jws";

GetOptions ('subject=s' => \$subject, 'message=s' => \$message, 'recipient=s' => \$recipient, 'uri=s' => \$uri);

SOAP::Lite
  -> uri($uri)
  -> proxy($uri)
  -> sendNotification(SOAP::Data->type(string => $recipient), 
  SOAP::Data->type(string => $subject), 
  SOAP::Data->type(string => $message));
