#!/usr/bin/env python3
#
# Log out session and delete session file.
#------------------------------------------------------------------------------#

import telethon.sync
from telethon import TelegramClient
import sys

api_id = sys.argv[1]
api_hash = sys.argv[2]
session_name = sys.argv[3]

client = TelegramClient(session_name, api_id, api_hash)
client.connect()
assert client.is_user_authorized()
client.log_out()
