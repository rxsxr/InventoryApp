{$MODE OBJFPC}
{$MODESWITCH ARRAYOPERATORS}

Unit TransP;
Interface 
  Uses 
    EntryP, EntrySet, Rng,
    Date,
    fpjson;

  Procedure MakeTrans
    ( OnID : String );

  Function TransJSON : TJsonData;

  Procedure ClearUsedDates;

Implementation
  
  Var 
    DateSet : Array Of TDate1 = ();

  Function InDateSet(Date : TDate1) : Boolean;
    Var CD : TDate1;
  Begin
    For CD in DateSet Do
      If Date = CD Then
        Exit(True);
    Exit(False);
  End;

  Procedure ClearUsedDates;
  Begin
    DateSet := [];
  End;

  Type TrnEntry = 
    Object
      IDName     : String;
      BuyPrice
    , SellPrice  : Integer;
      AmountSold : Integer;
      DateStamp  : TDate1;

      Procedure Generate;
      Procedure AddFromEntry(Entry : TEntry);
      Function  ToJson : TJsonData;
    End;
  Var
    EntryArray : Array of TrnEntry = ();

  Procedure TrnEntry.Generate;
  Var ID_Entry : TEntry;
  Begin
    ID_Entry := FindEntry(IDName);
    Self.AddFromEntry(ID_Entry);

    AmountSold := IRandAB(1,10);
  
    DateStamp.Rand;
    While InDateSet(DateStamp) Do
      DateStamp.Rand;

    DateSet := DateSet + [DateStamp];
  End;

  Procedure TrnEntry.AddFromEntry(Entry : TEntry);
  Begin
    BuyPrice  := Entry.BuyPrice;
    SellPrice := Entry.SellPrice;
  End;

  Procedure MakeTrans
    ( OnID : String );
  Var
    NewT : TrnEntry;
  Begin
    NewT.IDName := OnID;
    NewT.Generate;

    EntryArray := EntryArray + [NewT];
  End;

  Function  TrnEntry.ToJson : TJsonData;
  Var JS_Obj : TJsonObject;
  Begin
    JS_Obj := TJsonObject.Create;

    JS_Obj.Add('idName', IDName);
    JS_Obj.Add('dateStamp', DateStamp.ToString);
    JS_Obj.Add('buyPrice', IntToPrice(BuyPrice));
    JS_Obj.Add('sellPrice', IntToPrice(SellPrice));
    JS_Obj.Add('numSold', AmountSold);

    ToJson := JS_Obj;
  End;

  Function TransJson : TJSONData;
  Var 
    JA : TJSONArray;
    Entry : TrnEntry;
  Begin
    JA := TJSONArray.Create;

    For Entry in EntryArray Do
      JA.Add( Entry.ToJson );

    TransJSON := JA;
  End;

End.
