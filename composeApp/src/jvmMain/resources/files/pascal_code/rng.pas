{$MODE OBJFPC}

{ Misc. RNG utilities }
Unit RNG; 
Interface 
  Uses SysUtils;

  Const 
    { Constants for RNG }
    { Prices are in "cents", so multiply $ amount by 100 }
    PRICE_BOUNDS : Array[0..2] of Integer = (1*100, 100*100, 200*100);
    MARKUP       : Real                   = 1.2;
    STOCK_BOUNDS : Array[0..1] of Integer = (5 , 800);
    LOW_BOUNDS   : Array[0..1] of Integer = (10, 200);

  Function IRandAB(A, B : Integer) : Integer;

  Function RandBuy  : Integer;
  Function RandSell : Integer;

Implementation

  Function IRandAB(A, B : Integer) : Integer;
  Begin
    IRandAB := Random(B-A) + A;
  End;

  Function RandBuy  : Integer;
  Begin
    RandBuy := IRandAB(PRICE_BOUNDS[0], PRICE_BOUNDS[1]);
  End;

  Function RandSell : Integer;
  Begin
    RandSell := IRandAB(PRICE_BOUNDS[1], PRICE_BOUNDS[2]);
  End;

End.
