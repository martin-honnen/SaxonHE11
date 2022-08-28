////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.expr.number;

import net.sf.saxon.z.IntRangeSet;

/**
 * This class contains static utility methods to test whether a character is alphanumeric, as defined
 * by the rules of xsl:number: that is, whether it is in one of the Unicode categories
 * Nd, Nl, No, Lu, Ll, Lt, Lm or Lo
 *
 * In Saxon 9.9 the data has been regenerated from Unicode 10.0.0.
 */

public class Alphanumeric {

    private static final int[] zeroDigits = {
            0x0030, 0x0660, 0x06f0, 0x0966, 0x09e6, 0x0a66, 0x0ae6, 0x0b66, 0x0be6, 0x0c66, 0x0ce6,
            0x0d66, 0x0e50, 0x0ed0, 0x0f20, 0x1040, 0x17e0, 0x1810, 0x1946, 0x19d0, 0xff10,
            0x104a0, 0x107ce, 0x107d8, 0x107e2, 0x107ec, 0x107f6};

    // These data sets were generated from the Unicode 10.0.0 database using a custom stylesheet.
    // (copied below)

    private static final int[] startPoints = new int[]{
            0x0030, 0x0041, 0x0061, 0x00AA, 0x00B2, 0x00B5, 0x00B9, 0x00BC, 0x00C0, 0x00D8,
            0x00F8, 0x02C6, 0x02E0, 0x02EC, 0x02EE, 0x0370, 0x0376, 0x037A, 0x037F, 0x0386,
            0x0388, 0x038C, 0x038E, 0x03A3, 0x03F7, 0x048A, 0x0531, 0x0559, 0x0561, 0x05D0,
            0x05F0, 0x0620, 0x0660, 0x066E, 0x0671, 0x06D5, 0x06E5, 0x06EE, 0x06FF, 0x0710,
            0x0712, 0x074D, 0x07B1, 0x07C0, 0x07F4, 0x07FA, 0x0800, 0x081A, 0x0824, 0x0828,
            0x0840, 0x0860, 0x08A0, 0x08B6, 0x0904, 0x093D, 0x0950, 0x0958, 0x0966, 0x0971,
            0x0985, 0x098F, 0x0993, 0x09AA, 0x09B2, 0x09B6, 0x09BD, 0x09CE, 0x09DC, 0x09DF,
            0x09E6, 0x09F4, 0x09FC, 0x0A05, 0x0A0F, 0x0A13, 0x0A2A, 0x0A32, 0x0A35, 0x0A38,
            0x0A59, 0x0A5E, 0x0A66, 0x0A72, 0x0A85, 0x0A8F, 0x0A93, 0x0AAA, 0x0AB2, 0x0AB5,
            0x0ABD, 0x0AD0, 0x0AE0, 0x0AE6, 0x0AF9, 0x0B05, 0x0B0F, 0x0B13, 0x0B2A, 0x0B32,
            0x0B35, 0x0B3D, 0x0B5C, 0x0B5F, 0x0B66, 0x0B71, 0x0B83, 0x0B85, 0x0B8E, 0x0B92,
            0x0B99, 0x0B9C, 0x0B9E, 0x0BA3, 0x0BA8, 0x0BAE, 0x0BD0, 0x0BE6, 0x0C05, 0x0C0E,
            0x0C12, 0x0C2A, 0x0C3D, 0x0C58, 0x0C60, 0x0C66, 0x0C78, 0x0C80, 0x0C85, 0x0C8E,
            0x0C92, 0x0CAA, 0x0CB5, 0x0CBD, 0x0CDE, 0x0CE0, 0x0CE6, 0x0CF1, 0x0D05, 0x0D0E,
            0x0D12, 0x0D3D, 0x0D4E, 0x0D54, 0x0D58, 0x0D66, 0x0D7A, 0x0D85, 0x0D9A, 0x0DB3,
            0x0DBD, 0x0DC0, 0x0DE6, 0x0E01, 0x0E32, 0x0E40, 0x0E50, 0x0E81, 0x0E84, 0x0E87,
            0x0E8A, 0x0E8D, 0x0E94, 0x0E99, 0x0EA1, 0x0EA5, 0x0EA7, 0x0EAA, 0x0EAD, 0x0EB2,
            0x0EBD, 0x0EC0, 0x0EC6, 0x0ED0, 0x0EDC, 0x0F00, 0x0F20, 0x0F40, 0x0F49, 0x0F88,
            0x1000, 0x103F, 0x1050, 0x105A, 0x1061, 0x1065, 0x106E, 0x1075, 0x108E, 0x1090,
            0x10A0, 0x10C7, 0x10CD, 0x10D0, 0x10FC, 0x124A, 0x1250, 0x1258, 0x125A, 0x1260,
            0x128A, 0x1290, 0x12B2, 0x12B8, 0x12C0, 0x12C2, 0x12C8, 0x12D8, 0x1312, 0x1318,
            0x1369, 0x1380, 0x13A0, 0x13F8, 0x1401, 0x166F, 0x1681, 0x16A0, 0x16EE, 0x1700,
            0x170E, 0x1720, 0x1740, 0x1760, 0x176E, 0x1780, 0x17D7, 0x17DC, 0x17E0, 0x17F0,
            0x1810, 0x1820, 0x1880, 0x1887, 0x18AA, 0x18B0, 0x1900, 0x1946, 0x1970, 0x1980,
            0x19B0, 0x19D0, 0x1A00, 0x1A20, 0x1A80, 0x1A90, 0x1AA7, 0x1B05, 0x1B45, 0x1B50,
            0x1B83, 0x1BAE, 0x1C00, 0x1C40, 0x1C4D, 0x1C80, 0x1CE9, 0x1CEE, 0x1CF5, 0x1D00,
            0x1E00, 0x1F18, 0x1F20, 0x1F48, 0x1F50, 0x1F59, 0x1F5B, 0x1F5D, 0x1F5F, 0x1F80,
            0x1FB6, 0x1FBE, 0x1FC2, 0x1FC6, 0x1FD0, 0x1FD6, 0x1FE0, 0x1FF2, 0x1FF6, 0x2070,
            0x2074, 0x207F, 0x2090, 0x2102, 0x2107, 0x210A, 0x2115, 0x2119, 0x2124, 0x2126,
            0x2128, 0x212A, 0x212F, 0x213C, 0x2145, 0x214E, 0x2150, 0x2460, 0x24EA, 0x2776,
            0x2C00, 0x2C30, 0x2C60, 0x2CEB, 0x2CF2, 0x2CFD, 0x2D00, 0x2D27, 0x2D2D, 0x2D30,
            0x2D6F, 0x2D80, 0x2DA0, 0x2DA8, 0x2DB0, 0x2DB8, 0x2DC0, 0x2DC8, 0x2DD0, 0x2DD8,
            0x2E2F, 0x3005, 0x3021, 0x3031, 0x3038, 0x3041, 0x309D, 0x30A1, 0x30FC, 0x3105,
            0x3131, 0x3192, 0x31A0, 0x31F0, 0x3220, 0x3248, 0x3251, 0x3280, 0x32B1, 0x3400,
            0x4E00, 0xA000, 0xA4D0, 0xA500, 0xA610, 0xA640, 0xA67F, 0xA6A0, 0xA717, 0xA722,
            0xA78B, 0xA7B0, 0xA7F7, 0xA803, 0xA807, 0xA80C, 0xA830, 0xA840, 0xA882, 0xA8D0,
            0xA8F2, 0xA8FB, 0xA8FD, 0xA900, 0xA930, 0xA960, 0xA984, 0xA9CF, 0xA9E0, 0xA9E6,
            0xAA00, 0xAA40, 0xAA44, 0xAA50, 0xAA60, 0xAA7A, 0xAA7E, 0xAAB1, 0xAAB5, 0xAAB9,
            0xAAC0, 0xAAC2, 0xAADB, 0xAAE0, 0xAAF2, 0xAB01, 0xAB09, 0xAB11, 0xAB20, 0xAB28,
            0xAB30, 0xAB5C, 0xAB70, 0xABF0, 0xAC00, 0xD7B0, 0xD7CB, 0xF900, 0xFA70, 0xFB00,
            0xFB13, 0xFB1D, 0xFB1F, 0xFB2A, 0xFB38, 0xFB3E, 0xFB40, 0xFB43, 0xFB46, 0xFBD3,
            0xFD50, 0xFD92, 0xFDF0, 0xFE70, 0xFE76, 0xFF10, 0xFF21, 0xFF41, 0xFF66, 0xFFC2,
            0xFFCA, 0xFFD2, 0xFFDA, 0x10000, 0x1000D, 0x10028, 0x1003C, 0x1003F, 0x10050, 0x10080,
            0x10107, 0x10140, 0x1018A, 0x10280, 0x102A0, 0x102E1, 0x10300, 0x1032D, 0x10350, 0x10380,
            0x103A0, 0x103C8, 0x103D1, 0x10400, 0x104A0, 0x104B0, 0x104D8, 0x10500, 0x10530, 0x10600,
            0x10740, 0x10760, 0x10800, 0x10808, 0x1080A, 0x10837, 0x1083C, 0x1083F, 0x10858, 0x10879,
            0x108A7, 0x108E0, 0x108F4, 0x108FB, 0x10920, 0x10980, 0x109BC, 0x109D2, 0x10A10, 0x10A15,
            0x10A19, 0x10A40, 0x10A60, 0x10A80, 0x10AC0, 0x10AC9, 0x10AEB, 0x10B00, 0x10B40, 0x10B58,
            0x10B78, 0x10BA9, 0x10C00, 0x10C80, 0x10CC0, 0x10CFA, 0x10E60, 0x11003, 0x11052, 0x11083,
            0x110D0, 0x110F0, 0x11103, 0x11136, 0x11150, 0x11176, 0x11183, 0x111C1, 0x111D0, 0x111DC,
            0x111E1, 0x11200, 0x11213, 0x11280, 0x11288, 0x1128A, 0x1128F, 0x1129F, 0x112B0, 0x112F0,
            0x11305, 0x1130F, 0x11313, 0x1132A, 0x11332, 0x11335, 0x1133D, 0x11350, 0x1135D, 0x11400,
            0x11447, 0x11450, 0x11480, 0x114C4, 0x114C7, 0x114D0, 0x11580, 0x115D8, 0x11600, 0x11644,
            0x11650, 0x11680, 0x116C0, 0x11700, 0x11730, 0x118A0, 0x118FF, 0x11A00, 0x11A0B, 0x11A3A,
            0x11A50, 0x11A5C, 0x11A86, 0x11AC0, 0x11C00, 0x11C0A, 0x11C40, 0x11C50, 0x11C72, 0x11D00,
            0x11D08, 0x11D0B, 0x11D46, 0x11D50, 0x12000, 0x12400, 0x12480, 0x13000, 0x14400, 0x16800,
            0x16A40, 0x16A60, 0x16AD0, 0x16B00, 0x16B40, 0x16B50, 0x16B5B, 0x16B63, 0x16B7D, 0x16F00,
            0x16F50, 0x16F93, 0x16FE0, 0x17000, 0x18800, 0x1B000, 0x1B170, 0x1BC00, 0x1BC70, 0x1BC80,
            0x1BC90, 0x1D360, 0x1D400, 0x1D456, 0x1D49E, 0x1D4A2, 0x1D4A5, 0x1D4A9, 0x1D4AE, 0x1D4BB,
            0x1D4BD, 0x1D4C5, 0x1D507, 0x1D50D, 0x1D516, 0x1D51E, 0x1D53B, 0x1D540, 0x1D546, 0x1D54A,
            0x1D552, 0x1D6A8, 0x1D6C2, 0x1D6DC, 0x1D6FC, 0x1D716, 0x1D736, 0x1D750, 0x1D770, 0x1D78A,
            0x1D7AA, 0x1D7C4, 0x1D7CE, 0x1E800, 0x1E8C7, 0x1E900, 0x1E950, 0x1EE00, 0x1EE05, 0x1EE21,
            0x1EE24, 0x1EE27, 0x1EE29, 0x1EE34, 0x1EE39, 0x1EE3B, 0x1EE42, 0x1EE47, 0x1EE49, 0x1EE4B,
            0x1EE4D, 0x1EE51, 0x1EE54, 0x1EE57, 0x1EE59, 0x1EE5B, 0x1EE5D, 0x1EE5F, 0x1EE61, 0x1EE64,
            0x1EE67, 0x1EE6C, 0x1EE74, 0x1EE79, 0x1EE7E, 0x1EE80, 0x1EE8B, 0x1EEA1, 0x1EEA5, 0x1EEAB,
            0x1F100, 0x20000, 0x2A700, 0x2B740, 0x2B820, 0x2CEB0, 0x2F800};


//    private static int[] startPoints = new int[]{
//            0x0030, 0x0041, 0x0061, 0x00AA, 0x00B2, 0x00B5, 0x00B9, 0x00BC, 0x00C0, 0x00D8, 0x00F8,
//            0x0250, 0x02C6, 0x02E0, 0x02EE, 0x037A, 0x0386, 0x0388, 0x038C, 0x038E, 0x03A3, 0x03D0,
//            0x03F7, 0x048A, 0x04D0, 0x0500, 0x0531, 0x0559, 0x0561, 0x05D0, 0x05F0, 0x0621, 0x0640,
//            0x0660, 0x066E, 0x0671, 0x06D5, 0x06E5, 0x06EE, 0x06FF, 0x0710, 0x0712, 0x074D, 0x0780,
//            0x07B1, 0x0904, 0x093D, 0x0950, 0x0958, 0x0966, 0x097D, 0x0985, 0x098F, 0x0993, 0x09AA,
//            0x09B2, 0x09B6, 0x09BD, 0x09CE, 0x09DC, 0x09DF, 0x09E6, 0x09F4, 0x0A05, 0x0A0F, 0x0A13,
//            0x0A2A, 0x0A32, 0x0A35, 0x0A38, 0x0A59, 0x0A5E, 0x0A66, 0x0A72, 0x0A85, 0x0A8F, 0x0A93,
//            0x0AAA, 0x0AB2, 0x0AB5, 0x0ABD, 0x0AD0, 0x0AE0, 0x0AE6, 0x0B05, 0x0B0F, 0x0B13, 0x0B2A,
//            0x0B32, 0x0B35, 0x0B3D, 0x0B5C, 0x0B5F, 0x0B66, 0x0B71, 0x0B83, 0x0B85, 0x0B8E, 0x0B92,
//            0x0B99, 0x0B9C, 0x0B9E, 0x0BA3, 0x0BA8, 0x0BAE, 0x0BE6, 0x0C05, 0x0C0E, 0x0C12, 0x0C2A,
//            0x0C35, 0x0C60, 0x0C66, 0x0C85, 0x0C8E, 0x0C92, 0x0CAA, 0x0CB5, 0x0CBD, 0x0CDE, 0x0CE0,
//            0x0CE6, 0x0D05, 0x0D0E, 0x0D12, 0x0D2A, 0x0D60, 0x0D66, 0x0D85, 0x0D9A, 0x0DB3, 0x0DBD,
//            0x0DC0, 0x0E01, 0x0E32, 0x0E40, 0x0E50, 0x0E81, 0x0E84, 0x0E87, 0x0E8A, 0x0E8D, 0x0E94,
//            0x0E99, 0x0EA1, 0x0EA5, 0x0EA7, 0x0EAA, 0x0EAD, 0x0EB2, 0x0EBD, 0x0EC0, 0x0EC6, 0x0ED0,
//            0x0EDC, 0x0F00, 0x0F20, 0x0F40, 0x0F49, 0x0F88, 0x1000, 0x1023, 0x1029, 0x1040, 0x1050,
//            0x10A0, 0x10D0, 0x10FC, 0x1100, 0x115F, 0x11A8, 0x1200, 0x124A, 0x1250, 0x1258, 0x125A,
//            0x1260, 0x128A, 0x1290, 0x12B2, 0x12B8, 0x12C0, 0x12C2, 0x12C8, 0x12D8, 0x1312, 0x1318,
//            0x1369, 0x1380, 0x13A0, 0x1401, 0x166F, 0x1681, 0x16A0, 0x16EE, 0x1700, 0x170E, 0x1720,
//            0x1740, 0x1760, 0x176E, 0x1780, 0x17D7, 0x17DC, 0x17E0, 0x17F0, 0x1810, 0x1820, 0x1880,
//            0x1900, 0x1946, 0x1970, 0x1980, 0x19C1, 0x19D0, 0x1A00, 0x1D00, 0x1E00, 0x1EA0, 0x1F00,
//            0x1F18, 0x1F20, 0x1F48, 0x1F50, 0x1F59, 0x1F5B, 0x1F5D, 0x1F5F, 0x1F80, 0x1FB6, 0x1FBE,
//            0x1FC2, 0x1FC6, 0x1FD0, 0x1FD6, 0x1FE0, 0x1FF2, 0x1FF6, 0x2070, 0x2074, 0x207F, 0x2090,
//            0x2102, 0x2107, 0x210A, 0x2115, 0x2119, 0x2124, 0x2126, 0x2128, 0x212A, 0x212F, 0x2133,
//            0x213C, 0x2145, 0x2153, 0x2460, 0x24EA, 0x2776, 0x2C00, 0x2C30, 0x2C80, 0x2CFD, 0x2D00,
//            0x2D30, 0x2D6F, 0x2D80, 0x2DA0, 0x2DA8, 0x2DB0, 0x2DB8, 0x2DC0, 0x2DC8, 0x2DD0, 0x2DD8,
//            0x3005, 0x3021, 0x3031, 0x3038, 0x3041, 0x309D, 0x30A1, 0x30FC, 0x3105, 0x3131, 0x3192,
//            0x31A0, 0x31F0, 0x3220, 0x3251, 0x3280, 0x32B1, 0x3400, 0x4E00, /*0x9FBB,*/ 0xA000,
//            0xA800, 0xA803, 0xA807, 0xA80C, 0xAC00, /*0xD7A3,*/ 0xF900, 0xFA30, 0xFA70, 0xFB00, 0xFB13,
//            0xFB1D, 0xFB1F, 0xFB2A, 0xFB38, 0xFB3E, 0xFB40, 0xFB43, 0xFB46, 0xFBD3, 0xFD50, 0xFD92,
//            0xFDF0, 0xFE70, 0xFE76, 0xFF10, 0xFF21, 0xFF41, 0xFF66, 0xFFC2, 0xFFCA, 0xFFD2, 0xFFDA,
//            0x10000, 0x1000D, 0x10028, 0x1003C, 0x1003F, 0x10050, 0x10080, 0x10107, 0x10140, 0x1018A,
//            0x10300, 0x10320, 0x10330, 0x10380, 0x103A0, 0x103C8, 0x103D1, 0x10400, 0x104A0, 0x10800,
//            0x10808, 0x1080A, 0x10837, 0x1083C, 0x1083F, 0x10A00, 0x10A10, 0x10A15, 0x10A19, 0x10A40,
//            0x1D400, 0x1D456, 0x1D49E, 0x1D4A2, 0x1D4A5, 0x1D4A9, 0x1D4AE, 0x1D4BB, 0x1D4BD, 0x1D4C5,
//            0x1D507, 0x1D50D, 0x1D516, 0x1D51E, 0x1D53B, 0x1D540, 0x1D546, 0x1D54A, 0x1D552, 0x1D6A8,
//            0x1D6C2, 0x1D6DC, 0x1D6FC, 0x1D716, 0x1D736, 0x1D750, 0x1D770, 0x1D78A, 0x1D7AA, 0x1D7C4,
//            0x1D7CE, 0x20000, 0x2F800};

    private static final int[] endPoints = new int[]{
            0x0039, 0x005A, 0x007A, 0x00AA, 0x00B3, 0x00B5, 0x00BA, 0x00BE, 0x00D6, 0x00F6,
            0x02C1, 0x02D1, 0x02E4, 0x02EC, 0x02EE, 0x0374, 0x0377, 0x037D, 0x037F, 0x0386,
            0x038A, 0x038C, 0x03A1, 0x03F5, 0x0481, 0x052F, 0x0556, 0x0559, 0x0587, 0x05EA,
            0x05F2, 0x064A, 0x0669, 0x066F, 0x06D3, 0x06D5, 0x06E6, 0x06FC, 0x06FF, 0x0710,
            0x072F, 0x07A5, 0x07B1, 0x07EA, 0x07F5, 0x07FA, 0x0815, 0x081A, 0x0824, 0x0828,
            0x0858, 0x086A, 0x08B4, 0x08BD, 0x0939, 0x093D, 0x0950, 0x0961, 0x096F, 0x0980,
            0x098C, 0x0990, 0x09A8, 0x09B0, 0x09B2, 0x09B9, 0x09BD, 0x09CE, 0x09DD, 0x09E1,
            0x09F1, 0x09F9, 0x09FC, 0x0A0A, 0x0A10, 0x0A28, 0x0A30, 0x0A33, 0x0A36, 0x0A39,
            0x0A5C, 0x0A5E, 0x0A6F, 0x0A74, 0x0A8D, 0x0A91, 0x0AA8, 0x0AB0, 0x0AB3, 0x0AB9,
            0x0ABD, 0x0AD0, 0x0AE1, 0x0AEF, 0x0AF9, 0x0B0C, 0x0B10, 0x0B28, 0x0B30, 0x0B33,
            0x0B39, 0x0B3D, 0x0B5D, 0x0B61, 0x0B6F, 0x0B77, 0x0B83, 0x0B8A, 0x0B90, 0x0B95,
            0x0B9A, 0x0B9C, 0x0B9F, 0x0BA4, 0x0BAA, 0x0BB9, 0x0BD0, 0x0BF2, 0x0C0C, 0x0C10,
            0x0C28, 0x0C39, 0x0C3D, 0x0C5A, 0x0C61, 0x0C6F, 0x0C7E, 0x0C80, 0x0C8C, 0x0C90,
            0x0CA8, 0x0CB3, 0x0CB9, 0x0CBD, 0x0CDE, 0x0CE1, 0x0CEF, 0x0CF2, 0x0D0C, 0x0D10,
            0x0D3A, 0x0D3D, 0x0D4E, 0x0D56, 0x0D61, 0x0D78, 0x0D7F, 0x0D96, 0x0DB1, 0x0DBB,
            0x0DBD, 0x0DC6, 0x0DEF, 0x0E30, 0x0E33, 0x0E46, 0x0E59, 0x0E82, 0x0E84, 0x0E88,
            0x0E8A, 0x0E8D, 0x0E97, 0x0E9F, 0x0EA3, 0x0EA5, 0x0EA7, 0x0EAB, 0x0EB0, 0x0EB3,
            0x0EBD, 0x0EC4, 0x0EC6, 0x0ED9, 0x0EDF, 0x0F00, 0x0F33, 0x0F47, 0x0F6C, 0x0F8C,
            0x102A, 0x1049, 0x1055, 0x105D, 0x1061, 0x1066, 0x1070, 0x1081, 0x108E, 0x1099,
            0x10C5, 0x10C7, 0x10CD, 0x10FA, 0x1248, 0x124D, 0x1256, 0x1258, 0x125D, 0x1288,
            0x128D, 0x12B0, 0x12B5, 0x12BE, 0x12C0, 0x12C5, 0x12D6, 0x1310, 0x1315, 0x135A,
            0x137C, 0x138F, 0x13F5, 0x13FD, 0x166C, 0x167F, 0x169A, 0x16EA, 0x16F8, 0x170C,
            0x1711, 0x1731, 0x1751, 0x176C, 0x1770, 0x17B3, 0x17D7, 0x17DC, 0x17E9, 0x17F9,
            0x1819, 0x1877, 0x1884, 0x18A8, 0x18AA, 0x18F5, 0x191E, 0x196D, 0x1974, 0x19AB,
            0x19C9, 0x19DA, 0x1A16, 0x1A54, 0x1A89, 0x1A99, 0x1AA7, 0x1B33, 0x1B4B, 0x1B59,
            0x1BA0, 0x1BE5, 0x1C23, 0x1C49, 0x1C7D, 0x1C88, 0x1CEC, 0x1CF1, 0x1CF6, 0x1DBF,
            0x1F15, 0x1F1D, 0x1F45, 0x1F4D, 0x1F57, 0x1F59, 0x1F5B, 0x1F5D, 0x1F7D, 0x1FB4,
            0x1FBC, 0x1FBE, 0x1FC4, 0x1FCC, 0x1FD3, 0x1FDB, 0x1FEC, 0x1FF4, 0x1FFC, 0x2071,
            0x2079, 0x2089, 0x209C, 0x2102, 0x2107, 0x2113, 0x2115, 0x211D, 0x2124, 0x2126,
            0x2128, 0x212D, 0x2139, 0x213F, 0x2149, 0x214E, 0x2189, 0x249B, 0x24FF, 0x2793,
            0x2C2E, 0x2C5E, 0x2CE4, 0x2CEE, 0x2CF3, 0x2CFD, 0x2D25, 0x2D27, 0x2D2D, 0x2D67,
            0x2D6F, 0x2D96, 0x2DA6, 0x2DAE, 0x2DB6, 0x2DBE, 0x2DC6, 0x2DCE, 0x2DD6, 0x2DDE,
            0x2E2F, 0x3007, 0x3029, 0x3035, 0x303C, 0x3096, 0x309F, 0x30FA, 0x30FF, 0x312E,
            0x318E, 0x3195, 0x31BA, 0x31FF, 0x3229, 0x324F, 0x325F, 0x3289, 0x32BF, 0x4DB5,
            0x9FEA, 0xA48C, 0xA4FD, 0xA60C, 0xA62B, 0xA66E, 0xA69D, 0xA6EF, 0xA71F, 0xA788,
            0xA7AE, 0xA7B7, 0xA801, 0xA805, 0xA80A, 0xA822, 0xA835, 0xA873, 0xA8B3, 0xA8D9,
            0xA8F7, 0xA8FB, 0xA8FD, 0xA925, 0xA946, 0xA97C, 0xA9B2, 0xA9D9, 0xA9E4, 0xA9FE,
            0xAA28, 0xAA42, 0xAA4B, 0xAA59, 0xAA76, 0xAA7A, 0xAAAF, 0xAAB1, 0xAAB6, 0xAABD,
            0xAAC0, 0xAAC2, 0xAADD, 0xAAEA, 0xAAF4, 0xAB06, 0xAB0E, 0xAB16, 0xAB26, 0xAB2E,
            0xAB5A, 0xAB65, 0xABE2, 0xABF9, 0xD7A3, 0xD7C6, 0xD7FB, 0xFA6D, 0xFAD9, 0xFB06,
            0xFB17, 0xFB1D, 0xFB28, 0xFB36, 0xFB3C, 0xFB3E, 0xFB41, 0xFB44, 0xFBB1, 0xFD3D,
            0xFD8F, 0xFDC7, 0xFDFB, 0xFE74, 0xFEFC, 0xFF19, 0xFF3A, 0xFF5A, 0xFFBE, 0xFFC7,
            0xFFCF, 0xFFD7, 0xFFDC, 0x1000B, 0x10026, 0x1003A, 0x1003D, 0x1004D, 0x1005D, 0x100FA,
            0x10133, 0x10178, 0x1018B, 0x1029C, 0x102D0, 0x102FB, 0x10323, 0x1034A, 0x10375, 0x1039D,
            0x103C3, 0x103CF, 0x103D5, 0x1049D, 0x104A9, 0x104D3, 0x104FB, 0x10527, 0x10563, 0x10736,
            0x10755, 0x10767, 0x10805, 0x10808, 0x10835, 0x10838, 0x1083C, 0x10855, 0x10876, 0x1089E,
            0x108AF, 0x108F2, 0x108F5, 0x1091B, 0x10939, 0x109B7, 0x109CF, 0x10A00, 0x10A13, 0x10A17,
            0x10A33, 0x10A47, 0x10A7E, 0x10A9F, 0x10AC7, 0x10AE4, 0x10AEF, 0x10B35, 0x10B55, 0x10B72,
            0x10B91, 0x10BAF, 0x10C48, 0x10CB2, 0x10CF2, 0x10CFF, 0x10E7E, 0x11037, 0x1106F, 0x110AF,
            0x110E8, 0x110F9, 0x11126, 0x1113F, 0x11172, 0x11176, 0x111B2, 0x111C4, 0x111DA, 0x111DC,
            0x111F4, 0x11211, 0x1122B, 0x11286, 0x11288, 0x1128D, 0x1129D, 0x112A8, 0x112DE, 0x112F9,
            0x1130C, 0x11310, 0x11328, 0x11330, 0x11333, 0x11339, 0x1133D, 0x11350, 0x11361, 0x11434,
            0x1144A, 0x11459, 0x114AF, 0x114C5, 0x114C7, 0x114D9, 0x115AE, 0x115DB, 0x1162F, 0x11644,
            0x11659, 0x116AA, 0x116C9, 0x11719, 0x1173B, 0x118F2, 0x118FF, 0x11A00, 0x11A32, 0x11A3A,
            0x11A50, 0x11A83, 0x11A89, 0x11AF8, 0x11C08, 0x11C2E, 0x11C40, 0x11C6C, 0x11C8F, 0x11D06,
            0x11D09, 0x11D30, 0x11D46, 0x11D59, 0x12399, 0x1246E, 0x12543, 0x1342E, 0x14646, 0x16A38,
            0x16A5E, 0x16A69, 0x16AED, 0x16B2F, 0x16B43, 0x16B59, 0x16B61, 0x16B77, 0x16B8F, 0x16F44,
            0x16F50, 0x16F9F, 0x16FE1, 0x187EC, 0x18AF2, 0x1B11E, 0x1B2FB, 0x1BC6A, 0x1BC7C, 0x1BC88,
            0x1BC99, 0x1D371, 0x1D454, 0x1D49C, 0x1D49F, 0x1D4A2, 0x1D4A6, 0x1D4AC, 0x1D4B9, 0x1D4BB,
            0x1D4C3, 0x1D505, 0x1D50A, 0x1D514, 0x1D51C, 0x1D539, 0x1D53E, 0x1D544, 0x1D546, 0x1D550,
            0x1D6A5, 0x1D6C0, 0x1D6DA, 0x1D6FA, 0x1D714, 0x1D734, 0x1D74E, 0x1D76E, 0x1D788, 0x1D7A8,
            0x1D7C2, 0x1D7CB, 0x1D7FF, 0x1E8C4, 0x1E8CF, 0x1E943, 0x1E959, 0x1EE03, 0x1EE1F, 0x1EE22,
            0x1EE24, 0x1EE27, 0x1EE32, 0x1EE37, 0x1EE39, 0x1EE3B, 0x1EE42, 0x1EE47, 0x1EE49, 0x1EE4B,
            0x1EE4F, 0x1EE52, 0x1EE54, 0x1EE57, 0x1EE59, 0x1EE5B, 0x1EE5D, 0x1EE5F, 0x1EE62, 0x1EE64,
            0x1EE6A, 0x1EE72, 0x1EE77, 0x1EE7C, 0x1EE7E, 0x1EE89, 0x1EE9B, 0x1EEA3, 0x1EEA9, 0x1EEBB,
            0x1F10C, 0x2A6D6, 0x2B734, 0x2B81D, 0x2CEA1, 0x2EBE0, 0x2FA1D};


//    private static int[] endPoints = new int[]{
//            0x0039, 0x005A, 0x007A, 0x00AA, 0x00B3, 0x00B5, 0x00BA, 0x00BE, 0x00D6, 0x00F6, 0x0241,
//            0x02C1, 0x02D1, 0x02E4, 0x02EE, 0x037A, 0x0386, 0x038A, 0x038C, 0x03A1, 0x03CE, 0x03F5,
//            0x0481, 0x04CE, 0x04F9, 0x050F, 0x0556, 0x0559, 0x0587, 0x05EA, 0x05F2, 0x063A, 0x064A,
//            0x0669, 0x066F, 0x06D3, 0x06D5, 0x06E6, 0x06FC, 0x06FF, 0x0710, 0x072F, 0x076D, 0x07A5,
//            0x07B1, 0x0939, 0x093D, 0x0950, 0x0961, 0x096F, 0x097D, 0x098C, 0x0990, 0x09A8, 0x09B0,
//            0x09B2, 0x09B9, 0x09BD, 0x09CE, 0x09DD, 0x09E1, 0x09F1, 0x09F9, 0x0A0A, 0x0A10, 0x0A28,
//            0x0A30, 0x0A33, 0x0A36, 0x0A39, 0x0A5C, 0x0A5E, 0x0A6F, 0x0A74, 0x0A8D, 0x0A91, 0x0AA8,
//            0x0AB0, 0x0AB3, 0x0AB9, 0x0ABD, 0x0AD0, 0x0AE1, 0x0AEF, 0x0B0C, 0x0B10, 0x0B28, 0x0B30,
//            0x0B33, 0x0B39, 0x0B3D, 0x0B5D, 0x0B61, 0x0B6F, 0x0B71, 0x0B83, 0x0B8A, 0x0B90, 0x0B95,
//            0x0B9A, 0x0B9C, 0x0B9F, 0x0BA4, 0x0BAA, 0x0BB9, 0x0BF2, 0x0C0C, 0x0C10, 0x0C28, 0x0C33,
//            0x0C39, 0x0C61, 0x0C6F, 0x0C8C, 0x0C90, 0x0CA8, 0x0CB3, 0x0CB9, 0x0CBD, 0x0CDE, 0x0CE1,
//            0x0CEF, 0x0D0C, 0x0D10, 0x0D28, 0x0D39, 0x0D61, 0x0D6F, 0x0D96, 0x0DB1, 0x0DBB, 0x0DBD,
//            0x0DC6, 0x0E30, 0x0E33, 0x0E46, 0x0E59, 0x0E82, 0x0E84, 0x0E88, 0x0E8A, 0x0E8D, 0x0E97,
//            0x0E9F, 0x0EA3, 0x0EA5, 0x0EA7, 0x0EAB, 0x0EB0, 0x0EB3, 0x0EBD, 0x0EC4, 0x0EC6, 0x0ED9,
//            0x0EDD, 0x0F00, 0x0F33, 0x0F47, 0x0F6A, 0x0F8B, 0x1021, 0x1027, 0x102A, 0x1049, 0x1055,
//            0x10C5, 0x10FA, 0x10FC, 0x1159, 0x11A2, 0x11F9, 0x1248, 0x124D, 0x1256, 0x1258, 0x125D,
//            0x1288, 0x128D, 0x12B0, 0x12B5, 0x12BE, 0x12C0, 0x12C5, 0x12D6, 0x1310, 0x1315, 0x135A,
//            0x137C, 0x138F, 0x13F4, 0x166C, 0x1676, 0x169A, 0x16EA, 0x16F0, 0x170C, 0x1711, 0x1731,
//            0x1751, 0x176C, 0x1770, 0x17B3, 0x17D7, 0x17DC, 0x17E9, 0x17F9, 0x1819, 0x1877, 0x18A8,
//            0x191C, 0x196D, 0x1974, 0x19A9, 0x19C7, 0x19D9, 0x1A16, 0x1DBF, 0x1E9B, 0x1EF9, 0x1F15,
//            0x1F1D, 0x1F45, 0x1F4D, 0x1F57, 0x1F59, 0x1F5B, 0x1F5D, 0x1F7D, 0x1FB4, 0x1FBC, 0x1FBE,
//            0x1FC4, 0x1FCC, 0x1FD3, 0x1FDB, 0x1FEC, 0x1FF4, 0x1FFC, 0x2071, 0x2079, 0x2089, 0x2094,
//            0x2102, 0x2107, 0x2113, 0x2115, 0x211D, 0x2124, 0x2126, 0x2128, 0x212D, 0x2131, 0x2139,
//            0x213F, 0x2149, 0x2183, 0x249B, 0x24FF, 0x2793, 0x2C2E, 0x2C5E, 0x2CE4, 0x2CFD, 0x2D25,
//            0x2D65, 0x2D6F, 0x2D96, 0x2DA6, 0x2DAE, 0x2DB6, 0x2DBE, 0x2DC6, 0x2DCE, 0x2DD6, 0x2DDE,
//            0x3007, 0x3029, 0x3035, 0x303C, 0x3096, 0x309F, 0x30FA, 0x30FF, 0x312C, 0x318E, 0x3195,
//            0x31B7, 0x31FF, 0x3229, 0x325F, 0x3289, 0x32BF, 0x4DB5, 0x9FBB, /*0x9FBB,*/ 0xA48C,
//            0xA801, 0xA805, 0xA80A, 0xA822, /*0xAC00,*/ 0xD7A3, 0xFA2D, 0xFA6A, 0xFAD9, 0xFB06, 0xFB17,
//            0xFB1D, 0xFB28, 0xFB36, 0xFB3C, 0xFB3E, 0xFB41, 0xFB44, 0xFBB1, 0xFD3D, 0xFD8F, 0xFDC7,
//            0xFDFB, 0xFE74, 0xFEFC, 0xFF19, 0xFF3A, 0xFF5A, 0xFFBE, 0xFFC7, 0xFFCF, 0xFFD7, 0xFFDC,
//            0x1000B, 0x10026, 0x1003A, 0x1003D, 0x1004D, 0x1005D, 0x100FA, 0x10133, 0x10178, 0x1018A,
//            0x1031E, 0x10323, 0x1034A, 0x1039D, 0x103C3, 0x103CF, 0x103D5, 0x1049D, 0x104A9, 0x10805,
//            0x10808, 0x10835, 0x10838, 0x1083C, 0x1083F, 0x10A00, 0x10A13, 0x10A17, 0x10A33, 0x10A47,
//            0x1D454, 0x1D49C, 0x1D49F, 0x1D4A2, 0x1D4A6, 0x1D4AC, 0x1D4B9, 0x1D4BB, 0x1D4C3, 0x1D505,
//            0x1D50A, 0x1D514, 0x1D51C, 0x1D539, 0x1D53E, 0x1D544, 0x1D546, 0x1D550, 0x1D6A5, 0x1D6C0,
//            0x1D6DA, 0x1D6FA, 0x1D714, 0x1D734, 0x1D74E, 0x1D76E, 0x1D788, 0x1D7A8, 0x1D7C2, 0x1D7C9,
//            0x1D7FF, 0x2A6D6, 0x2FA1D};

    /*@NotNull*/ private static final IntRangeSet alphanumerics = new IntRangeSet(startPoints, endPoints);

    /**
     * Determine whether a Unicode codepoint is alphanumeric, that is, whether it is in one of the
     * categories Nd, Nl, No, Lu, Ll, Lt, Lm or Lo
     *
     * @param codepoint the codepoint to be tested
     * @return true if the codepoint is in one of these categories
     */

    public static boolean isAlphanumeric(int codepoint) {
        return alphanumerics.contains(codepoint);
    }

    /**
     * Determine whether a character represents a decimal digit and if so, which digit.
     *
     * @param in the Unicode character being tested.
     * @return -1 if it's not a decimal digit, otherwise the digit value.
     */

    public static int getDigitValue(int in) {
        for (int zeroDigit : zeroDigits) {
            if (in <= zeroDigit + 9) {
                if (in >= zeroDigit) {
                    return in - zeroDigit;
                } else {
                    return -1;
                }
            }
        }
        return -1;
    }

    /**
     * Determine which digit family a decimal digit belongs to: that is, return the corresponding zero digit.
     *
     * @param in a Unicode character
     * @return if the character is a digit, return the Unicode character that represents zero in the same digit
     *         family. Otherwise, return -1.
     */

    public static int getDigitFamily(int in) {
        for (int zeroDigit : zeroDigits) {
            if (in <= zeroDigit + 9) {
                if (in >= zeroDigit) {
                    return zeroDigit;
                } else {
                    return -1;
                }
            }
        }
        return -1;

    }

    private Alphanumeric() {
    }
}

// For completeness, here is the stylesheet used to generate these lists of ranges from UnicodeData.txt:
// Updated copy in repo/tools/unicode/alphanumeric-ranges-10.xsl

//<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
//   xmlns:xs="http://www.w3.org/2001/XMLSchema"
//   xmlns:f="http://saxonica.com/ns/unicode"
//   exclude-result-prefixes="xs f"
//>
//
//<!-- Output a list of the start and end points of contiguous ranges of characters
//     classified as letters or digits.
//
//     Note this doesn't handle the CJK Extended Ideograph ranges A and B, 3400-4DB5 and 20000-2A6D6,
//     which have to be edited in by hand. Also 4E00-9FBB and AC00-D7A3
//-->
//
//<xsl:output method="text"/>
//<xsl:variable name="data" select="doc('UnicodeData.xml')"/>
//
//<xsl:function name="f:isAlphaNum" as="xs:boolean">
//  <xsl:param name="char" as="element(Char)"/>
//  <xsl:sequence select="$char/Field3=('Nd', 'Nl', 'No', 'Lu', 'Ll', 'Lt', 'Lm', 'Lo')"/>
//</xsl:function>
//
//<xsl:function name="f:hexToInt" as="xs:integer?">
//  <xsl:param name="hex" as="xs:string?"/>
//  <xsl:sequence select="if (empty($hex)) then () else Integer:parseInt($hex, 16)"
//                xmlns:Integer="java:java.lang.Integer"/>
//</xsl:function>
//
//<xsl:param name="p"/>
//<xsl:template name="test">
//  <xsl:value-of select="f:hexToInt($p)"/>
//</xsl:template>
//
//<xsl:template name="main">
//
//    <xsl:text>int[] startPoints = new int[]{</xsl:text>
//    <xsl:for-each-group select="$data/*/Char" group-adjacent="concat(f:isAlphaNum(.), f:hexToInt(code) - position())">
//      <xsl:if test="f:isAlphaNum(.)">
//	      <xsl:text>0x</xsl:text>
//	      <xsl:value-of select="current-group()[1]/code"/>
//	      <xsl:text>, </xsl:text>
//	      <xsl:if test="position() mod 10 = 0">&#xa;</xsl:if>
//	    </xsl:if>
//    </xsl:for-each-group>
//    <xsl:text>};&#xa;&#xa;</xsl:text>
//    <xsl:text>int[] endPoints = new int[]{</xsl:text>
//    <xsl:for-each-group select="$data/*/Char" group-adjacent="concat(f:isAlphaNum(.), f:hexToInt(code) - position())">
//      <xsl:if test="f:isAlphaNum(.)">
//	      <xsl:text>0x</xsl:text>
//	      <xsl:value-of select="current-group()[last()]/code"/>
//	      <xsl:text>, </xsl:text>
//	      <xsl:if test="position() mod 10 = 0">&#xa;</xsl:if>
//	    </xsl:if>
//    </xsl:for-each-group>
//    <xsl:text>};&#xa;&#xa;</xsl:text>
//
//</xsl:template>
//
//
//</xsl:stylesheet>
