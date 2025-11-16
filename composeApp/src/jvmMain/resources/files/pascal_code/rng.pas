

{ Misc. RNG utilities }
Unit RNG; 
Interface 
  Type TDate1 = 
    Record 
      Year  : Integer;
      Month : 1 .. 12;
      Day   : 1 .. 31;
    End;

  Function IRandAB(A, B : Integer) : Integer;

  Function RandDate : TDate1;

Implementation

  Function IRandAB(A, B : Integer) : Integer;
  Begin
    IRandAB := Random(B-A) + A;
  End;

  Function RandDate : TDate1;
  Begin
    With RandDate Do
      Begin
      Year  := 2025;
      Month := IRandAB(5, 10);
      Day   := IRandAB(1, 29);
      End;
  End;
End.
