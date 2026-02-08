# ELG RFID
Elegoo Canvas RFID Programming<br>
<br>

The tags required are <a href=https://www.nxp.com/products/NTAG213_215_216>NTAG213</a> or <a href=https://www.nxp.com/products/NTAG213_215_216>NTAG215</a>.<br><br>


The Canvas is programmed to read 45 pages (0 to 44) of the tag to verify filament data.<br>
Because this data spans a 144-byte range, you must use a tag that supports at least that many pages.<br>
Although Elegoo uses the NTAG213 as standard, the NTAG215 and NTAG216 work perfectly as well since they offer even more storage while maintaining the same page structure.<br>



<br><br><br>
The android app is available on google play<br>
<a href="https://play.google.com/store/apps/details?id=dngsoftware.elgrfid&hl=en"><img src=https://github.com/DnG-Crafts/ELG-RFID/blob/main/docs/gp.webp width="30%" height="30%"></a>
<br>


## Tag Format

<br><br>

### URI Section 

This data is only for smartphone compatibility. <br>
It allows a phone to open the Elegoo website when tapped against the spool, but the printer itself does not use this information to identify the filament.<br>
The tag does not need this data present on the tag for the printer to read the filament section and identify the filament.<br>
<br>

| Page (Dec) | Byte 0 | Byte 1 | Byte 2 | Byte 3 | Field Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **4** | 01 | 03 | A0 | 0C | NDEF Structure |
| **5** | 34 | 03 | 0F | D1 | 03 NDEF Message; 0F length (15 bytes) |
| **6** | 01 | 0B | 55 | 04 | 55 Record Type "U" (URI); 04 URI Prefix (https://) |
| **7** | 65 | 6C | 65 | 67 | ASCII for "**eleg**" |
| **8** | 6F | 6F | 2E | 63 | ASCII for "**oo.c**" |
| **9** | 6F | 6D | FE | 00 | ASCII for "**om**";  FE Terminator (TLV) |
| **10** | 00 | 00 | 00 | 00 | Reserved For URI |
| **11** | 00 | 00 | 00 | 00 | Reserved For URI |
| **12** | 00 | 00 | 00 | 00 | Reserved For URI |
| **13** | 00 | 00 | 00 | 00 | Reserved For URI |
| **14** | 00 | 00 | 00 | 00 | Reserved For URI |
| **15** | 00 | 00 | 00 | 00 | Reserved For URI |



<br><br>


### Filament Section 

This data is what the printer uses to identify the filament.<br>
<br>

| Page (Dec) | Byte 0 | Byte 1 | Byte 2 | Byte 3 | Field Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **16** | 36 | EE | EE | EE | Header |
| **17** | EE | 00 | 00 | 00 | Manufacturer ID |
| **18** | 00 | 80 | 76 | 65 | Material Type ("PLA") |
| **19** | 00 | 04 | 00 | 00 | Material Position (PLA-CF") |
| **20** | FF | 37 | 00 | FF | Color Code (#FF3700) |
| **21** | 00 | D2 | 00 | F0 | Extuder Temp Min / Max (210 / 240) |
| **22** | 00 | 00 | 00 | 00 | Unknown - Bed Temps? |
| **23** | 00 | AF | 03 | E8 | Diameter / Weight (175 / 1000) |
| **24** | 00 | 36 | C8 | 00 | Unknown - Production Code?  |


<br>
<br>

You can view a full tag dump <a href=https://github.com/DnG-Crafts/ELG-RFID/blob/main/docs/README.md>Here</a>

<br>