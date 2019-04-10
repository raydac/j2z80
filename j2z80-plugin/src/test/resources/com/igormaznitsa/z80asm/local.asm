    org #1000
    djnz @loop
    DEFM "test"
@loop:  NOP
@loop: NOP
    DEFM "test"
    djnz @loop
    end
