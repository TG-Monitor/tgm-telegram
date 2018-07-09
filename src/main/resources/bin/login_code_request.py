#!/usr/bin/env python3
#
# Initiate login by requesting a login code to be sent to the user's device
# (SMS or Telegram).
#
# Prints the 'phone code hash' to stdout that is needed for the subsequent
# 'sign_in' method.
#------------------------------------------------------------------------------#

import telethon.sync
from telethon import TelegramClient
import sys

api_id = sys.argv[1]
api_hash = sys.argv[2]
session_name = sys.argv[3]
phone = sys.argv[4]

client = TelegramClient(session_name, api_id, api_hash)
client.connect()
assert not client.is_user_authorized()
res = client.send_code_request(phone)
# Example 'res' object (display with 'print(res.stringify())'):
# SentCode(
#         type=SentCodeTypeSms(
#                 length=5
#         ),
#         phone_code_hash='c6ff14a654682544ed',
#         phone_registered=True,
#         next_type=CodeTypeCall(
#         ),
#         timeout=120,
#         terms_of_service=None
# )
print(res.phone_code_hash)
