SFTP-Server
===========

FTPClient
---------
The client should either
* take in 3 parameters, FTPClient < server-ip > < server-port > DIR
* take in 4 parameters, FTPClient < server-ip > < server-port > GET < path of file to get >
* take in 5 parameters, FTPClient < server-ip > < server-port > PUT < path of file to put in client > [< path to put in server >]

FTPServer
---------
The server should
* take in 1 parameter, FTPServer < control-port >
* take in 2 parameters, FTPServer < control-port > [< data-port >]

A passive FTP mode is used to implement the FTPClient and FTPServer; FTPClient will send a PASV command to the FTPServer, for which
the server will reply with port and ip details to establish the connection.

