#!/usr/bin/perl -w
use SOAP::Lite;
use Getopt::Long;

my $uri = "http://localhost:8080/notification/SendSOAPNotification.jws";

my $subject = '';
my $message = '';
my $recipient = '';

my $host = 'localhost';
my $port = 2954;
my $alarm = '';

GetOptions ('subject=s' => \$subject, 
			'message=s' => \$message, 
			'recipient=s' => \$recipient, 
			'uri=s' => \$uri,
			'host=s' => \$host,
			'port=i' => \$port,
			'alarm=s' => \$alarm);

#  (String memberName, String summary, String message,
#			String OVHost, int OVPort, String alarmUUID)

SOAP::Lite
  -> uri($uri)
  -> proxy($uri)
  -> sendHPOVNNMNotification(SOAP::Data->type(string => $recipient), 
  SOAP::Data->type(string => $subject), 
  SOAP::Data->type(string => $message),
  SOAP::Data->type(string => $host),
  SOAP::Data->type(int => $port),
  SOAP::Data->type(string => $alarm),
  );
