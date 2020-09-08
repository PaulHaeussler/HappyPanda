#HappyPanda
#### A downloader for exhentai.org to download and archive your favourite hentai

This executable takes several arguments for startup to define its environment and runtime procedure.
A mysql database has to be provided with its credentials(if concerned for security, a localhost database should work fine) 
and a schema to use. This database will keep track of all albums and images, as well as all tags and metadata. 

Also required is a text file containing what the program uses in its requests headers to exhentai. This is necessary in 
order to appear as a normal browser. Entries are separated by five '=' as a delimiter. The only entry which usually requires
changes is "cookie". Either read your cookie and replace the anonymized info, or open the developer tools in your browser,
navigate to exhentai.org (provided you already have access with this browser) and check what headers your browser used.
This is usually done on the network tab, you're interested in the results of the return of exhentai.org itself.

### Program parameters:
Static parameters are always preceded with a dash '-', whereas variable parameters are not.

#####Mandatory parameters:
-   -host : the network address where the database is found, can be domain or IP
-   -dbname : name of the database schema to use. This has to be set up beforehand, as the program will only run operations
            on this schema and not access the rest of the database 
-   -user : username to be used to sign on database, has to have full permissions on the schema specified.
-   -pw : the password corresponding to the username
-   -p : A path to the directory in which the images should be placed 

#####Voluntary parameters:

-   -getAlbum : downloads one specified album with all its metadata, full URL has to be specified
-   -getFavs : downloads all your favorites, and maintains categories


A sample program call could look like this:
```
java -jar HappyPanda_V0.1.jar -host 127.0.0.1 -dbname happyPanda -user admin -pw 12345678 -p C:\hentai -getFavs
```

