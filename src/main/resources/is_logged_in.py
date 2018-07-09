#!/usr/bin/env python3
#
# Test whether current session is in an authorised (logged in) state or not.
#
# The script prints "true" if logged in, and "false" if not logged in.
#------------------------------------------------------------------------------#

import telethon.sync
from telethon import TelegramClient
import sys
import os.path

api_id = sys.argv[1]
api_hash = sys.argv[2]
session_name = sys.argv[3]

if not os.path.exists(session_name + ".session"):
    print("false")
else:
    client = TelegramClient(session_name, api_id, api_hash)
    client.connect()
    if client.is_user_authorized():
        print("true")
    else:
        print("false")
