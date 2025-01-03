'''
MIPS-32 Instruction Level Simulatr

CSE261 UNIST
run.py
'''

import util
import initialize
import ctypes


def OPCODE(INST):
    return ctypes.c_short(INST.opcode).value


def SET_OPCODE(INST, VAL):
    INST.opcode = ctypes.c_short(VAL).value


def FUNC(INST):
    return ctypes.c_short(INST.func_code).value


def SET_FUNC(INST, VAL):
    INST.func_code = ctypes.c_short(VAL).value


def RS(INST):
    return ctypes.c_ubyte(INST.rs).value


def SET_RS(INST, VAL):
    INST.rs = ctypes.c_ubyte(VAL).value


def RT(INST):
    return ctypes.c_ubyte(INST.rt).value


def SET_RT(INST, VAL):
    INST.rt = ctypes.c_ubyte(VAL).value


def RD(INST):
    return ctypes.c_ubyte(INST.rd).value


def SET_RD(INST, VAL):
    INST.rd = ctypes.c_ubyte(VAL).value


def FS(INST):
    return RD(INST)


def SET_FS(INST, VAL):
    SET_RD(INST, VAL)


def FT(INST):
    return RT(INST)


def SET_FT(INST, VAL):
    SET_RT(INST, VAL)


def FD(INST):
    return SHAMT(INST)


def SET_FD(INST, VAL):
    SET_SHAMT(INST, VAL)


def SHAMT(INST):
    return INST.shamt


def SET_SHAMT(INST, VAL):
    INST.shamt = ctypes.c_ubyte(VAL).value


def IMM(INST):
    return INST.imm


def SET_IMM(INST, VAL):
    INST.imm = ctypes.c_short(VAL).value


def BASE(INST):
    return RS(INST)


def SET_BASE(INST, VAL):
    SET_RS(INST, VAL)


def IOFFSET(INST):
    return IMM(INST)


def SET_IOFFSET(INST, VAL):
    SET_IMM(INST, VAL)


def IDISP(INST):
    X = INST.imm << 2
    return SIGN_EX(X)


def COND(INST):
    return RS(INST)


def SET_COND(INST, VAL):
    SET_RS(INST, VAL)


def CC(INST):
    return ((RT(INST)&0xffffffff) >> 2)


def ND(INST):
    return ((RT(INST) & 0x2) >> 1)


def TF(INST):
    return (RT(INST) & 0x1)


def TARGET(INST):
    return INST.target


def SET_TARGET(INST, VAL):
    INST.target = VAL


def ENCODING(INST):
    return INST.encoding


def SET_ENCODIGN(INST, VAL):
    INST.encoding = VAL


def EXPR(INST):
    return INST.expr


def SET_EXPR(INST, VAL):
    INST.expr = VAL


def SOURCE(INST):
    return INST.source_line


def SET_SOURCE(INST, VAL):
    INST.source_line = VAL


# Sign Extension
def SIGN_EX(X):
    if (X) & 0x8000:
        return X | 0xffff0000
    else:
        return X
def UNSIGNED(X):
    if (X) & 0x8000:
        return X & 0xffffffff
    else:
        return X 
        

COND_UN = 0x1
COND_EQ = 0x2
COND_LT = 0x4
COND_IN = 0x8

# Minimum and maximum values that fit in instruction's imm field
IMM_MIN = 0xffff8000
IMM_MAX = 0x00007fff

UIMM_MIN = 0
UIMM_MAX = (1 << 16)-1


def BRANCH_INST(TEST, TARGET):
    if TEST:
        target = TARGET
        JUMP_INST(target)


def JUMP_INST(TARGET):
    import util
    util.CURRENT_STATE.PC = TARGET


def LOAD_INST(LD, MASK):
    return (LD & (MASK))


# Procedure: get_inst_info
# Purpose: Read instruction information
def get_inst_info(pc):
    return initialize.INST_INFO[(pc - util.MEM_TEXT_START)>> 2]


# Procedure: process_instruction
# Purpose: Process one instruction
def process_instruction():
    # * Your implementation here *
    pass

# /***************************************************************/
# /*                                                             */
# /* Procedure: IFetch_Stage                                     */
# /*                                                             */
# /* Purpose: Instruction fetch                                  */
# /*                                                             */
# /***************************************************************/
def IFetch_Stage():
    pass


# /***************************************************************/
# /*                                                             */
# /* Procedure: IDecode_Stage                                    */
# /*                                                             */
# /* Purpose: Instruction decode                                 */
# /*                                                             */
# /***************************************************************/
def IDecode_Stage():
    pass


# /***************************************************************/
# /*                                                             */
# /* Procedure: Execute_Stage                                    */
# /*                                                             */
# /* Purpose: Instruction execution                              */
# /*                                                             */
# /***************************************************************/
def Execute_Stage():
    pass


# /***************************************************************/
# /*                                                             */
# /* Procedure: Memory_Stage                                     */
# /*                                                             */
# /* Purpose: Memory related execution                           */
# /*                                                             */
# /***************************************************************/
def Memory_Stage():
    pass


# /***************************************************************/
# /*                                                             */
# /* Procedure: WriteBack_Stage                                  */
# /*                                                             */
# /* Purpose: Write back related execution                       */
# /*                                                             */
# /***************************************************************/
def WriteBack_Stage():
    pass


# /***************************************************************/
# /*                                                             */
# /* Procedure: Choose_PC                                        */
# /*                                                             */
# /* Purpose: Choose corret PC among 3 candidates                */
# /*                                                             */
# /***************************************************************/
def Choose_PC():
    pass


# /***************************************************************/
# /*                                                             */
# /* Procedure: Flush_By_Jump                                    */
# /*                                                             */
# /* Purpose: Flush IF, ID stage                                 */
# /*                                                             */
# /***************************************************************/
def Flush_By_Jump():
    pass


# /***************************************************************/
# /*                                                             */
# /* Procedure: Flush_By_Branch_EX                               */
# /*                                                             */
# /* Purpose: Flush IF, ID stage                                 */
# /*          and stall IF, ID stage if prediction bit is unset  */
# /*                                                             */
# /***************************************************************/
def Flush_By_Branch_EX():
    pass


# /***************************************************************/
# /*                                                             */
# /* Procedure: Flush_By_Branch_MEM                              */
# /*                                                             */
# /* Purpose: Flush IF, ID, and EX stage                         */
# /*      and stall IF, ID, EX stage if prediction bit is unset  */
# /*                                                             */
# /***************************************************************/
def Flush_By_Branch_MEM():
    pass


# /***************************************************************/
# /*                                                             */
# /* Procedure: Flush_By_Branch                                  */
# /*                                                             */
# /* Purpose: Flush IF, ID, EX, MEM stage                        */
# /*                                                             */
# /***************************************************************/
def Flush_By_Branch():
    pass