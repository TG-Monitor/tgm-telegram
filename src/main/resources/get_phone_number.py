#!/usr/bin/env python3
#
# Get the phone number of the user associated with the specified session.
#------------------------------------------------------------------------------#

import telethon.sync
from telethon import TelegramClient
import sys

api_id = sys.argv[1]
api_hash = sys.argv[2]
session_name = sys.argv[3]

client = TelegramClient(session_name, api_id, api_hash)
client.connect()
print("+" + client.get_me().phone)
