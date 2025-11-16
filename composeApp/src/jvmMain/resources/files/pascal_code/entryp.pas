{$MODE OBJFPC}

Unit EntryP;
Interface
  Uses 
    SysUtils, StrUtils,
    fpjson,
    Rng,
    Classes; { For TStrings }
  Const 
    { Constants for initialization }
    NILs : String = '<<NIL>>';
    NILint : Integer = -1;
    NILflt : Real    = -1.0;

    { Constants for RNG }
    { Prices are in "cents", so multiply $ amount by 100 }
    PRICE_BOUNDS : Array[0..2] of Integer = (1*100, 100*100, 200*100);
    MARKUP       : Real                   = 1.2;
    STOCK_BOUNDS : Array[0..1] of Integer = (5 , 800);
    LOW_BOUNDS   : Array[0..1] of Integer = (10, 200);

  Type 
    TEntry = 
      Object
        GName, IDName : String;
        Tags : Array of String;
        BuyPrice, SellPrice : Integer;
        LowBound : Integer;
        StockAmount : Integer;
        SoldAmount  : Integer;
        UnitString  : String;
        UnitAmount  : Real;
        Constructor Init;
        Constructor InitFromStrings(ts : TStrings);
        Procedure GenerateExtras;
        Function  ToJson : TJsonData;
      End;

Implementation

  Constructor TEntry.Init;
  Begin
    GName  := NILs;
    IDName := NILs;
    SetLength(Tags, 0);

    BuyPrice  := NILint;
    SellPrice := NILint;

    LowBound    := NILint;
    StockAmount := NILint;
    SoldAmount  := NILint;

    UnitString := NILs;
    UnitAmount := NILflt;
  End;

  Constructor TEntry.InitFromStrings(ts : TStrings);
    Var I : Integer;
    { NOTE: This has side-effects }
    Function HasNameE(name : String) : Boolean;
    Begin
      I := Ts.IndexOfName(name);
      HasNameE := (I <> -1);
    End;
  
    Procedure AddTags(FromStr : String);
    Var 
      NumTags : Integer = 1;
      I : Integer = 0;
      C : Char;
    Begin
    { WriteLn('Getting tags from ', QuotedStr(FromStr)); }
      For C in FromStr Do
        If C = ',' Then Inc(NumTags);
  
      SetLength(Tags, NumTags);
      
      For I := 1 To NumTags Do
        Self.Tags[I-1] := Trim(ExtractDelimited(I, FromStr, [',']));
    End;

    Function ReadPrice(CV : String) : Integer;
    Var flt : Real;
    Begin
      CV  := TrimSet(CV, ['$', ' ']);
      { WriteLn('Read price ', QuotedStr(CV)); }
      Flt := StrToFloat(CV);
      ReadPrice := Trunc(100 * Flt);
    End;

    Function CVal : String;
    Begin CVal := TS.ValueFromIndex[I]; End;
  
    Procedure ErrorRequired(Key : String);
    Var C : Char;
    Begin
      WriteLn('Error: Field ' + QuotedStr(Key) + ' is required for ' + idName);
      WriteLn('Press Any key to exit');
      Read(C);
    End;
  Begin
    Self.Init; { nullify everything first }
    If HasNameE('gName')      Then GName := CVal;
    If HasNameE('idName')     Then IDName := CVal;
    If HasNameE('tags')       Then AddTags(CVal); 
    If HasNameE('buyPrice')   Then BuyPrice  := ReadPrice(CVal);
    If HasNameE('sellPrice')  Then SellPrice := ReadPrice(CVal);
    If HasNameE('lowBound')   Then LowBound := StrToInt(CVal);

    If HasNameE('unitString') Then UnitString := CVal;
    If HasNameE('unitAmount') Then UnitAmount := StrToFloat(CVal);

    If HasNameE('stockAmount') Then StockAmount := StrToInt(CVal);
    if HasNameE('soldAmount')  Then SoldAmount  := StrToInt(CVal);

    if GName  = NILs Then ErrorRequired('gName');
    If idName = NILs Then ErrorRequired('idName');
    { if (TS.IndexOfName('tags') = -1) Then ErrorRequired('tags'); }
  End;

  Procedure TEntry.GenerateExtras;
  Begin
    If BuyPrice = NILint Then 
      Begin
      BuyPrice     := IRandAB(PRICE_BOUNDS[0], PRICE_BOUNDS[1]);
      If SellPrice = NILint Then 
        SellPrice    := Round( MARKUP * BuyPrice );
      End;

    If LowBound = NILint Then LowBound := IRandAB(LOW_BOUNDS[0], LOW_BOUNDS[1]);
    If StockAmount = NILint Then StockAmount := IRandAB(STOCK_BOUNDS[0], STOCK_BOUNDS[1]);
  End;

  Function IntToPrice(X:Integer) : String;
  Begin
    IntToPrice := Format('$%d.%.2d', [X div 100, X mod 100]);
  End;

  Function  TEntry.ToJson : TJsonData;
  Var 
    JSO    : TJsonObject;
    JSTags : TJsonArray;
    Tag    : String;
  Begin
    JSO    := TJsonObject.Create;
    JSTags := TJsonArray.Create;

    For Tag in Tags Do
      JSTags.Add(Tag);

    JSO.Add('g_name', gName);
    JSO.Add('id_name', idName);
    JSO.Add('tags', JSTags);
    JSO.Add('buy_price', IntToPrice(BuyPrice));
    JSO.Add('sell_price', IntToPrice(SellPrice));
    JSO.Add('low_bound', LowBound);
    JSO.Add('stock_amount', StockAmount);

    ToJson := JSO;
  End;
End.
