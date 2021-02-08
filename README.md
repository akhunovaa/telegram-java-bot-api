# Telegram Bot API SERVER on JAVA

The Telegram Bot API SERVER provides a gRPC `(gRPC Remote Procedure Calls is an open source remote procedure call (RPC) system initially developed at Google in 2015. It uses HTTP/2 for transport)` API for [Telegram Bots](https://core.telegram.org/bots).


## IMPORTANT
This project is under development and and this is just the beginning :-)

## Table of Contents
- [Installation](#installation)
- [Usage](#usage)
- [Documentation](#documentation)
- [Moving a bot to a local server](#switching)
- [Moving a bot from one local server to another](#moving)
- [License](#license)

<a name="installation"></a>
## Installation

The simplest way to build and deploy `Telegram Bot API JAVA server` is to use [Kubernetes (K8s)](https://kubernetes.io/) with [HELM](https://helm.sh). 
Two pipeline files are located in project `Jenkinsfile4Docker` and `Jenkinsfile4Helm`.

<a name="usage"></a>
## Usage

Use `telegram-bot-api --help` to receive the list of all available options of the Telegram Bot API server.

The only mandatory options are `--api-id` and `--api-hash`. You must obtain your own `api_id` and `api_hash`
as described in https://core.telegram.org/api/obtaining_api_id and specify them using the `--api-id` and `--api-hash` options
or the `TELEGRAM_API_ID` and `TELEGRAM_API_HASH` environment variables.
[App configuration](https://my.telegram.org/) and enter them in application properties file:

```
########################TELEGRAM CLIENT#######################
telegram.client.path.location=tdlib
telegram.client.api.id=ENC(IhNpX/LGZLr7Uh+JUNkTMg==)
telegram.client.api.hash=ENC(1pk0rHkSMBRur9alDKTLn5nKr7FGXX5n6+wl8o3vLwT0bK5iK2sFHmBL2gUQMLNN)
telegram.client.system.language.code=en
telegram.client.device.model=JTServer
telegram.client.application.version=${jenkins.buildNumber}
```

Enable Bot API features not available at `https://api.telegram.org`. In the local mode the Bot API server allows to:
* Download files without a size limit.
* Upload files up to 2000 MB.
* Upload files using their local path and [the file URI scheme](https://en.wikipedia.org/wiki/File_URI_scheme).
* Use a very fast and modern [gRPC](https://grpc.io/) instead of old HTTP 1.1
* Use any local IP address.
* Use any port for the server.
* Receive the absolute local path as a value of the *file_path* field without the need to download the file after a *getFile* request.

By default the Telegram Bot API server is launched on the port 7499, which can be changed.

<a name="documentation"></a>
## Documentation
See [Bots: An introduction for developers](https://core.telegram.org/bots) for a brief description of Telegram Bots and their features.

See the [Telegram Bot API documentation](https://core.telegram.org/bots/api) for a description of the Bot API interface and a complete list of available classes, methods and updates.

Subscribe to [@BotNews](https://t.me/botnews) to be the first to know about the latest updates and join the discussion in [@BotTalk](https://t.me/bottalk).

<a name="switching"></a>
## Moving a bot to a local server

To guarantee that your bot will receive all updates, you must deregister it with the `https://api.telegram.org` server by calling the method [logOut](https://core.telegram.org/bots/api#logout).
After the bot is logged out, you can replace the address to which the bot sends requests with the address of your local server and use it in the usual way.

<a name="moving"></a>
## Moving a bot from one local server to another

If the bot is logged in on more than one server simultaneously, there is no guarantee that it will receive all updates.
To move a bot from one local server to another you can use the method [logOut](https://core.telegram.org/bots/api#logout) to log out on the old server before switching to the new one.

If you want to avoid losing updates between logging out on the old server and launching on the new server, you can remove the bot's webhook using the method
[deleteWebhook](https://core.telegram.org/bots/api#deletewebhook), then use the method [close](https://core.telegram.org/bots/api#close) to close the bot instance.
After the instance is closed, locate the bot's subdirectory in the working directory of the old server by the bot's user ID, move the subdirectory to the working directory of the new server
and continue sending requests to the new server as usual.


<a name="license"></a>
## License
`Telegram Bot API JAVA` source code is licensed under the terms of the Boost Software License. See [LICENSE_1_0.txt](http://www.boost.org/LICENSE_1_0.txt) for more information.
