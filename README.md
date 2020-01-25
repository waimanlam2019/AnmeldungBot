Anmeldung bot is a bot to search for "Anmeldung" for user who need a resident registration in Berlin.

This bot was written in the Lunar New Year day because a friend asked for help.

I used to used a ruby script from internet and it didn't work anymore.

My friend has some requirement because he prefer an anmeldung date as soon as possible. This would require the bot to also check for the date of the appointment.

How it work
===========
This bot used Selenium WebDriver in combination with Chrome Driver to simulate a human being browse the Berlin Anmeldung Website (https://service.berlin.de/terminvereinbarung/termin/day/) . It check for date showed as available in the website. ( Red date is unavailable, while a blue date comes with a link to the available anmeldung appointment burgeramt. ). If a good date shows up because someone released their slot, the bot would pop up an alert box and ask for confirmation. Human would check to see if the location is desirable. My friend only prefer burgeramt in west Berlin.


Background
==========
While I was in Berlin I also had to register as a resident by "anmeldung". The government require one to register himself/herself within a few weeks. However, usually the appointment slot in the coming days are taken already. Luckily, there is a special mechanism that people could release their slot if they want. Usually people just sit in front of a computer and refresh the page. I don't want to do that because it might take several hour to get one appointment, so I check for automated solution online. I found an ruby script to do it. I customised it a bit with a auto date checker. Today I rewritten it with Java because it no longer work.

Technical Difficulties
======================
The Berlin Anmeldung website used to be easy for bot. But my bot runs into Bot Verification sometimes. I help I could add OCR in the program but it is not practical. Just restart the app is okay.

The calender link used to contain the date of the appointment. For example the blue link "01" in the calender would contain something like 2020-Feb-01 in the href attribute of the a tag. However, it was gone. Now I have to click into the link and verify the "Datum" column with the program. Fortunately it is easy.

My friend also has a wishlist of preferred location of the burgeramt, such as Tempelhof and Schöneberg because it is easier to drive around these district. I also added this checking function since the appointment page contain the list of burgeramt available. I get the text and check if they fit.
