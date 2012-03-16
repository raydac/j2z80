package j80.cpu;

interface Z80debug {

	public static final String[] opc1 = {
		"00 NOP         ","01 LD BC,nn    ","02 LD (BC),A   ","03 INC BC      ",
		"04 INC B       ","05 DEC B       ","06 LD B,n      ","07 RLCA        ",
		"08 EX AF,AF'   ","09 ADD HL,BC   ","0A LD A,(BC)   ","0B DEC BC      ",
		"0C INC C       ","0D DEC C       ","0E LD C,n      ","0F RRCA        ",
		"10 DJNZ,of     ","11 LD DE,nn    ","12 LD (DE),A   ","13 INC DE      ",
		"14 INC D       ","15 DEC D       ","16 LD D,n      ","17 RLA         ",
		"18 JR of       ","19 ADD HL,DE   ","1A LD A,(DE)   ","1B DEC DE      ",
		"1C INC E       ","1D DEC E       ","1E LD E,n      ","1F RRA         ",
		"20 JR NZ,of    ","21 LD HL,nn    ","22 LD (nn),HL  ","23 INC HL      ",
		"24 INC H       ","25 DEC H       ","26 LD H,n      ","27 DAA         ",
		"28 JR Z,of     ","29 ADD HL,HL   ","2A LD HL,(nn)  ","2B DEC HL      ",
		"2C INC L       ","2D DEC L       ","2E LD L,n      ","2F CPL         ",
		"30 JR NC,of    ","31 LD SP,nn    ","32 LD (nn),A   ","33 INC SP      ",
		"34 INC (HL)    ","35 DEC (HL)    ","36 LD (HL),n   ","37 SCF         ",
		"38 JR C,of     ","39 ADD HL,SP   ","3A LD A,(nn)   ","3B DEC SP      ",
		"3C INC A       ","3D DEC A       ","3E LD A,n      ","3F CCF         ",
		"40 LD B,B      ","41 LD B,C      ","42 LD B,D      ","43 LD B,E      ",
		"44 LD B,H      ","45 LD B,L      ","46 LD B,(HL)   ","47 LD B,A      ",
		"48 LD C,B      ","49 LD C,C      ","4A LD C,D      ","4B LD C,E      ",
		"4C LD C,H      ","4D LD C,L      ","4E LD C,(HL)   ","4F LD C,A      ",
		"50 LD D,B      ","51 LD D,C      ","52 LD D,D      ","53 LD D,E      ",
		"54 LD D,H      ","55 LD D,L      ","56 LD D,(HL)   ","57 LD D,A      ",
		"58 LD E,B      ","59 LD E,C      ","5A LD E,D      ","5B LD E,E      ",
		"5C LD E,H      ","5D LD E,L      ","5E LD E,(HL)   ","5F LD E,A      ",
		"60 LD H,B      ","61 LD H,C      ","62 LD H,D      ","63 LD H,E      ",
		"64 LD H,H      ","65 LD H,L      ","66 LD H,(HL)   ","67 LD H,A      ",
		"68 LD L,B      ","69 LD L,C      ","6A LD L,D      ","6B LD L,E      ",
		"6C LD L,H      ","6D LD L,L      ","6E LD L,(HL)   ","6F LD L,A      ",
		"70 LD (HL),B   ","71 LD (HL),C   ","72 LD (HL),D   ","73 LD (HL),E   ",
		"74 LD (HL),H   ","75 LD (HL),L   ","76 HALT        ","77 LD (HL),A   ",
		"78 LD A,B      ","79 LD A,C      ","7A LD A,D      ","7B LD A,E      ",
		"7C LD A,H      ","7D LD A,L      ","7E LD A,(HL)   ","7F LD A,A      ",
		"80 ADD A,B     ","81 ADD A,C     ","82 ADD A,D     ","83 ADD A,E     ",
		"84 ADD A,H     ","85 ADD A,L     ","86 ADD A,(HL)  ","87 ADD A,A     ",
		"88 ADC A,B     ","89 ADC A,C     ","8A ADC A,D     ","8B ADC A,E     ",
		"8C ADC A,H     ","8D ADC A,L     ","8E ADC A,(HL)  ","8F ADC A,A     ",
		"90 SUB B       ","91 SUB C       ","92 SUB D       ","93 SUB E       ",
		"94 SUB H       ","95 SUB L       ","96 SUB (HL)    ","97 SUB A       ",
		"98 SBC A,B     ","99 SBC A,C     ","9A SBC A,D     ","9B SBC A,E     ",
		"9C SBC A,H     ","9D SBC A,L     ","9E SBC A,(HL)  ","9F SBC A,A     ",
		"A0 AND B       ","A1 AND C       ","A2 AND D       ","A3 AND E       ",
		"A4 AND H       ","A5 AND L       ","A6 AND (HL)    ","A7 AND A       ",
		"A8 XOR B       ","A9 XOR C       ","AA XOR D       ","AB XOR E       ",
		"AC XOR H       ","AD XOR L       ","AE XOR (HL)    ","AF XOR A       ",
		"B0 OR B        ","B1 OR C        ","B2 OR D        ","B3 OR E        ",
		"B4 OR H        ","B5 OR L        ","B6 OR (HL)     ","B7 OR A        ",
		"B8 CP B        ","B9 CP C        ","BA CP D        ","BB CP E        ",
		"BC CP H        ","BD CP L        ","BE CP (HL)     ","BF CP A        ",
		"C0 RET NZ      ","C1 POP BC      ","C2 JP NZ,nn    ","C3 JP nn       ",
		"C4 CALL NZ,nn  ","C5 PUSH BC     ","C6 ADD A,n     ","C7 RST &00     ",
		"C8 RET Z       ","C9 RET         ","CA JP Z,nn     ","CB -->2        ",
		"CC CALL Z,nn   ","CD CALL nn     ","CE ADC A,n     ","CF RST &08     ",
		"D0 RET NC      ","D1 POP DE      ","D2 JP NC,nn    ","D3 OUT (n),A   ",
		"D4 CALL NC,nn  ","D5 PUSH DE     ","D6 SUB n       ","D7 RST &10     ",
		"D8 RET C       ","D9 EXX         ","DA JP C,nn     ","DB IN A,(n)    ",
		"DC CALL C,nn   ","DD -->IX       ","DE SBC A,n     ","DF RST &18     ",
		"E0 RET PO      ","E1 POP HL      ","E2 JP PO,nn    ","E3 EX (SP),HL  ",
		"E4 CALL PO,nn  ","E5 PUSH HL     ","E6 AND n       ","E7 RST &20     ",
		"E8 RET PE      ","E9 JP (HL)     ","EA JP PE,nn    ","EB EX DE,HL    ",
		"EC CALL PE,nn  ","ED -->3        ","EE XOR n       ","EF RST &28     ",
		"F0 RET P       ","F1 POP AF      ","F2 JP P,nn     ","F3 DI          ",
		"F4 CALL P,nn   ","F5 PUSH AF     ","F6 OR n        ","F7 RST &30     ",
		"F8 RET M       ","F9 LD SP,HL    ","FA JP M,nn     ","FB EI          ",
		"FC CALL M,nn   ","FD -->IY       ","FE CP n        ","FF RST &38     "
	};

	public static final String[] opc2 = {
		"00 ???????????",  "01 ???????????",  "02 ???????????",  "03 ???????????",
		"04 ???????????",  "05 ???????????",  "06 ???????????",  "07 ???????????",
		"08 ???????????",  "09 ???????????",  "0A ???????????",  "0B ???????????",
		"0C ???????????",  "0D ???????????",  "0E ???????????",  "0F ???????????",
		"10 ???????????",  "11 ???????????",  "12 ???????????",  "13 ???????????",
		"14 ???????????",  "15 ???????????",  "16 ???????????",  "17 ???????????",
		"18 ???????????",  "19 ???????????",  "1A ???????????",  "1B ???????????",
		"1C ???????????",  "1D ???????????",  "1E ???????????",  "1F ???????????",
		"20 ???????????",  "21 ???????????",  "22 ???????????",  "23 ???????????",
		"24 ???????????",  "25 ???????????",  "26 ???????????",  "27 ???????????",
		"28 ???????????",  "29 ???????????",  "2A ???????????",  "2B ???????????",
		"2C ???????????",  "2D ???????????",  "2E ???????????",  "2F ???????????",
		"30 ???????????",  "31 ???????????",  "32 ???????????",  "33 ???????????",
		"34 ???????????",  "35 ???????????",  "36 ???????????",  "37 ???????????",
		"38 ???????????",  "39 ???????????",  "3A ???????????",  "3B ???????????",
		"3C ???????????",  "3D ???????????",  "3E ???????????",  "3F ???????????",
		"40 BIT 0,B",	   "41 BIT 0,C", 	  "42 BIT 0,D",		 "43 BIT 0,E",
		"44 BIT 0,H", 	   "45 BIT 0,L",  	  "46 BIT 0,(HL)",   "47 BIT 0,A",
		"48 BIT 1,B",  	   "49 BIT 1,C",  	  "4A BIT 1,D",  	 "4B BIT 1,E",
		"4C BIT 1,H",  	   "4D BIT 1,L",  	  "4E BIT 1,(HL)",   "4F BIT 1,A",
		"50 BIT 2,B",  	   "51 BIT 2,C",  	  "52 BIT 2,D",  	 "53 BIT 2,E",
		"54 BIT 2,H",  	   "55 BIT 2,L",  	  "56 BIT 2,(HL)",   "57 BIT 2,A",
		"58 BIT 3,B",  	   "59 BIT 3,C",  	  "5A BIT 3,D",  	 "5B BIT 3,E",
		"5C BIT 3,H",  	   "5D BIT 3,L",  	  "5E BIT 3,(HL)",   "5F BIT 3,A",
		"60 BIT 4,B",  	   "61 BIT 4,C",  	  "62 BIT 4,D",  	 "63 BIT 4,E",
		"64 BIT 4,H",  	   "65 BIT 4,L",  	  "66 BIT 4,(HL)",   "67 BIT 4,A",
		"68 BIT 5,B",  	   "69 BIT 5,C",  	  "6A BIT 5,D",  	 "6B BIT 5,E",
		"6C BIT 5,H",  	   "6D BIT 5,L",  	  "6E BIT 5,(HL)",   "6F BIT 5,A",
		"70 BIT 6,B",  	   "71 BIT 6,C",  	  "72 BIT 6,D",  	 "73 BIT 6,E",
		"74 BIT 6,H",  	   "75 BIT 6,L",  	  "76 BIT 6,(HL)",   "77 BIT 6,A",
		"78 BIT 7,B",  	   "79 BIT 7,C",  	  "7A BIT 7,D",  	 "7B BIT 7,E",
		"7C BIT 7,H",  	   "7D BIT 7,L",  	  "7E BIT 7,(HL)",   "7F BIT 7,A",
		"80 ???????????",  "81 ???????????",  "82 ???????????",  "83 ???????????",
		"84 ???????????",  "85 ???????????",  "86 ???????????",  "87 ???????????",
		"88 ???????????",  "89 ???????????",  "8A ???????????",  "8B ???????????",
		"8C ???????????",  "8D ???????????",  "8E ???????????",  "8F ???????????",
		"90 ???????????",  "91 ???????????",  "92 ???????????",  "93 ???????????",
		"94 ???????????",  "95 ???????????",  "96 ???????????",  "97 ???????????",
		"98 ???????????",  "99 ???????????",  "9A ???????????",  "9B ???????????",
		"9C ???????????",  "9D ???????????",  "9E ???????????",  "9F ???????????",
		"A0 ???????????",  "A1 ???????????",  "A2 ???????????",  "A3 ???????????",
		"A4 ???????????",  "A5 ???????????",  "A6 ???????????",  "A7 ???????????",
		"A8 ???????????",  "A9 ???????????",  "AA ???????????",  "AB ???????????",
		"AC ???????????",  "AD ???????????",  "AE ???????????",  "AF ???????????",
		"B0 ???????????",  "B1 ???????????",  "B2 ???????????",  "B3 ???????????",
		"B4 ???????????",  "B5 ???????????",  "B6 ???????????",  "B7 ???????????",
		"B8 ???????????",  "B9 ???????????",  "BA ???????????",  "BB ???????????",
		"BC ???????????",  "BD ???????????",  "BE ???????????",  "BF ???????????",
		"C0 ???????????",  "C1 ???????????",  "C2 ???????????",  "C3 ???????????",
		"C4 ???????????",  "C5 ???????????",  "C6 ???????????",  "C7 ???????????",
		"C8 ???????????",  "C9 ???????????",  "CA ???????????",  "CB ???????????",
		"CC ???????????",  "CD ???????????",  "CE ???????????",  "CF ???????????",
		"D0 ???????????",  "D1 ???????????",  "D2 ???????????",  "D3 ???????????",
		"D4 ???????????",  "D5 ???????????",  "D6 ???????????",  "D7 ???????????",
		"D8 ???????????",  "D9 ???????????",  "DA ???????????",  "DB ???????????",
		"DC ???????????",  "DD ???????????",  "DE ???????????",  "DF ???????????",
		"E0 ???????????",  "E1 ???????????",  "E2 ???????????",  "E3 ???????????",
		"E4 ???????????",  "E5 ???????????",  "E6 ???????????",  "E7 ???????????",
		"E8 ???????????",  "E9 ???????????",  "EA ???????????",  "EB ???????????",
		"EC ???????????",  "ED ???????????",  "EE ???????????",  "EF ???????????",
		"F0 ???????????",  "F1 ???????????",  "F2 ???????????",  "F3 ???????????",
		"F4 ???????????",  "F5 ???????????",  "F6 ???????????",  "F7 ???????????",
		"F8 ???????????",  "F9 ???????????",  "FA ???????????",  "FB ???????????",
		"FC ???????????",  "FD ???????????",  "FE ???????????",  "FF ???????????"
	};

	public static final String[] opc3 = {
		"00 ???????????",  "01 ???????????",  "02 ???????????",  "03 ???????????",
		"04 ???????????",  "05 ???????????",  "06 ???????????",  "07 ???????????",
		"08 ???????????",  "09 ???????????",  "0A ???????????",  "0B ???????????",
		"0C ???????????",  "0D ???????????",  "0E ???????????",  "0F ???????????",
		"10 ???????????",  "11 ???????????",  "12 ???????????",  "13 ???????????",
		"14 ???????????",  "15 ???????????",  "16 ???????????",  "17 ???????????",
		"18 ???????????",  "19 ???????????",  "1A ???????????",  "1B ???????????",
		"1C ???????????",  "1D ???????????",  "1E ???????????",  "1F ???????????",
		"20 ???????????",  "21 ???????????",  "22 ???????????",  "23 ???????????",
		"24 ???????????",  "25 ???????????",  "26 ???????????",  "27 ???????????",
		"28 ???????????",  "29 ???????????",  "2A ???????????",  "2B ???????????",
		"2C ???????????",  "2D ???????????",  "2E ???????????",  "2F ???????????",
		"30 ???????????",  "31 ???????????",  "32 ???????????",  "33 ???????????",
		"34 ???????????",  "35 ???????????",  "36 ???????????",  "37 ???????????",
		"38 ???????????",  "39 ???????????",  "3A ???????????",  "3B ???????????",
		"3C ???????????",  "3D ???????????",  "3E ???????????",  "3F ???????????",
		"40 ???????????",  "41 ???????????",  "42 ???????????",  "ED43 LD (nn),BC",
		"44 ???????????",  "45 ???????????",  "46 ???????????",  "ED47 LD I,A",
		"48 ???????????",  "49 ???????????",  "4A ???????????",  "4B ???????????",
		"4C ???????????",  "4D ???????????",  "4E ???????????",  "4F ???????????",
		"50 ???????????",  "51 ???????????",  "52 ???????????",  "ED53 LD (nn),DE",
		"54 ???????????",  "55 ???????????",  "56 ???????????",  "ED57 LD A,I",
		"58 ???????????",  "59 ???????????",  "5A ???????????",  "5B ???????????",
		"5C ???????????",  "5D ???????????",  "ED5E IM 2",       "5F ???????????",
		"60 ???????????",  "61 ???????????",  "62 ???????????",  "ED63 LD (nn),HL",
		"64 ???????????",  "65 ???????????",  "66 ???????????",  "67 ???????????",
		"68 ???????????",  "69 ???????????",  "6A ???????????",  "6B ???????????",
		"6C ???????????",  "6D ???????????",  "6E ???????????",  "6F ???????????",
		"70 ???????????",  "71 ???????????",  "72 ???????????",  "ED73 LD (nn),SP",
		"74 ???????????",  "75 ???????????",  "76 ???????????",  "77 ???????????",
		"78 ???????????",  "79 ???????????",  "7A ???????????",  "7B ???????????",
		"7C ???????????",  "7D ???????????",  "7E ???????????",  "7F ???????????",
		"80 ???????????",  "81 ???????????",  "82 ???????????",  "83 ???????????",
		"84 ???????????",  "85 ???????????",  "86 ???????????",  "87 ???????????",
		"88 ???????????",  "89 ???????????",  "8A ???????????",  "8B ???????????",
		"8C ???????????",  "8D ???????????",  "8E ???????????",  "8F ???????????",
		"90 ???????????",  "91 ???????????",  "92 ???????????",  "93 ???????????",
		"94 ???????????",  "95 ???????????",  "96 ???????????",  "97 ???????????",
		"98 ???????????",  "99 ???????????",  "9A ???????????",  "9B ???????????",
		"9C ???????????",  "9D ???????????",  "9E ???????????",  "9F ???????????",
		"A0 ???????????",  "A1 ???????????",  "A2 ???????????",  "A3 ???????????",
		"A4 ???????????",  "A5 ???????????",  "A6 ???????????",  "A7 ???????????",
		"A8 ???????????",  "A9 ???????????",  "AA ???????????",  "AB ???????????",
		"AC ???????????",  "AD ???????????",  "AE ???????????",  "AF ???????????",
		"EDB0 LDIR",       "B1 ???????????",  "B2 ???????????",  "B3 ???????????",
		"B4 ???????????",  "B5 ???????????",  "B6 ???????????",  "B7 ???????????",
		"B8 ???????????",  "B9 ???????????",  "BA ???????????",  "BB ???????????",
		"BC ???????????",  "BD ???????????",  "BE ???????????",  "BF ???????????",
		"C0 ???????????",  "C1 ???????????",  "C2 ???????????",  "C3 ???????????",
		"C4 ???????????",  "C5 ???????????",  "C6 ???????????",  "C7 ???????????",
		"C8 ???????????",  "C9 ???????????",  "CA ???????????",  "CB ???????????",
		"CC ???????????",  "CD ???????????",  "CE ???????????",  "CF ???????????",
		"D0 ???????????",  "D1 ???????????",  "D2 ???????????",  "D3 ???????????",
		"D4 ???????????",  "D5 ???????????",  "D6 ???????????",  "D7 ???????????",
		"D8 ???????????",  "D9 ???????????",  "DA ???????????",  "DB ???????????",
		"DC ???????????",  "DD ???????????",  "DE ???????????",  "DF ???????????",
		"E0 ???????????",  "E1 ???????????",  "E2 ???????????",  "E3 ???????????",
		"E4 ???????????",  "E5 ???????????",  "E6 ???????????",  "E7 ???????????",
		"E8 ???????????",  "E9 ???????????",  "EA ???????????",  "EB ???????????",
		"EC ???????????",  "ED ???????????",  "EE ???????????",  "EF ???????????",
		"F0 ???????????",  "F1 ???????????",  "F2 ???????????",  "F3 ???????????",
		"F4 ???????????",  "F5 ???????????",  "F6 ???????????",  "F7 ???????????",
		"F8 ???????????",  "F9 ???????????",  "FA ???????????",  "FB ???????????",
		"FC ???????????",  "FD ???????????",  "FE ???????????",  "FF ???????????"
	};
}
