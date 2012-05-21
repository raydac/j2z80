com.igormaznitsa.impl.MemoryFillerNative.fillArea#[Lcom.igormaznitsa.j2z80test.MainSAbstractTemplateGenjII]V:
    CLRLOC

    LD D,(IX-2+1) ; address of the AbstractTemplateGenerator object
    LD E,(IX-2)

    LD H,(IX-4+1) ; the start address into HL
    LD L,(IX-4)

    LD B,(IX-6+1) ; the length into BC
    LD C,(IX-6)

    ; just fill
@FILLLOOP:
    LD A,B
    OR C
    JP Z,@END

    PUSH DE
    PUSH BC
    PUSH HL
    
    PUSH DE ; place objref
    PUSH HL ; place address
;---------------------------------
     ; get class id for the objectref
     LD HL,#2
     ADD HL,SP ; the objref address in HL
     LD C,(HL) ; load the objref into BC
     INC HL
     LD B,(HL)
     CALL ___GET_OBJECT_CLASS_ID ; get the class id into BC
     LD HL,com.igormaznitsa.j2z80test.MainSAbstractTemplateGen.getValueForAddress#[I]I_VT_REC
     CALL __PREPAREINVOKEVIRTUAL ; HL contains record address start, BC contains object class id
     ; HL contains address and BC contains the full frame size in bytes
     LD (@INVOKE_JMP+1),HL
     LD A,4 ; load argument area length in bytes
     CALL ___BEFORE_INVOKE ; prepare the frame
@INVOKE_JMP:
     CALL 0
     CALL ___AFTER_INVOKE ; restore the previous frame
;-----------------------------------
     POP HL

     LD (HL),C
     INC HL
     LD (HL),B
     INC HL

     POP BC

     DEC BC
     DEC BC

     POP DE

    JP @FILLLOOP
@END:
    RET
