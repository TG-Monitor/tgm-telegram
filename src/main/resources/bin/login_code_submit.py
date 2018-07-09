#!/usr/bin/env python3
#
# Finalise login by signing the user in with the login code that the user must
# have received on their device and entered into the system.
#
# If this scripts exits with value 0, then the login was successful.
#------------------------------------------------------------------------------#

import telethon.sync
from telethon import TelegramClient
import sys

api_id = sys.argv[1]
api_hash = sys.argv[2]
session_name = sys.argv[3]
phone = sys.argv[4]
login_code = sys.argv[5]
phone_code_hash = sys.argv[6]

client = TelegramClient(session_name, api_id, api_hash)
client.connect()
assert not client.is_user_authorized()
client.sign_in(phone=phone, code=login_code, phone_code_hash=phone_code_hash)
assert client.is_user_authorized()
