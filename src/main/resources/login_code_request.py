#!/usr/bin/env python3
#
# Start login procedure by requesting a login code to be sent to the user's
# device (by SMS or Telegram).
#
# The script writes the "phone code hash" to stdout, which is later needed to
# complete the login procedure.
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
