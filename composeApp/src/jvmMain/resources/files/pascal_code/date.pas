{$MODE OBJFPC}

Unit Date;
Interface 
  Uses Rng, StrUtils, SysUtils;
  Const
    C_MONTHS : Array[0..1] of Integer = (5,10);
    C_DAYS   : Array[0..1] of Integer = (1,29);
    C_YEARS  : Array[0..1] of Integer = (2024, 2025);

  Type TDate1 = 
    Object 
      Year  : Integer;
      Month : 1 .. 12;
      Day   : 1 .. 31;
      Constructor Init;
      Constructor Rand;
      Constructor FromString(S:String);
      Function ToString : String;
      Function IsNIL : Boolean;
    End;

  Operator =(A, B:TDate1) R : Boolean;

Implementation
  Constructor TDate1.Init;
  Begin
    Year   := 0;
    Month  := 1;
    Day    := 1;
  End;

  Constructor TDate1.Rand;
  Begin
    Year  := IRandAB(C_YEARS[0] , C_YEARS[1]);
    Month := IRandAB(C_MONTHS[0], C_MONTHS[1]);
    Day   := IRandAB(C_DAYS[0]  , C_DAYS[1]);
  End;

  Constructor TDate1.FromString(S:String);
  Var YP, MP, DP : String;
  Begin
    YP := ExtractDelimited(1, S, ['-']);
    MP := ExtractDelimited(2, S, ['-']);
    DP := ExtractDelimited(3, S, ['-']);
    Year   := StrToInt(YP);
    Month  := StrToInt(MP);
    Day    := StrToInt(DP);
  End;

  Function TDate1.ToString : String;
  Begin
    ToString := Format('%.4d-%.2d-%.2d', [Year, Month, Day]);
  End;

  Function TDate1.IsNIL : Boolean;
  Begin
    IsNIL := (Year = 0) And (Month = 1) And (Day = 1);
  End;

  Operator =(A, B:TDate1) R : Boolean;
  Begin
    R := 
      (A.Year = B.Year) And 
      (A.Month = B.Month) And 
      (A.Day = B.Day) ;
  End;
End.
