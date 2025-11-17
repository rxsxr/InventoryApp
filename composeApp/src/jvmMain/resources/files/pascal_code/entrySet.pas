

Unit EntrySet;
Interface 
  Uses EntryP, fpjson;
  
  Procedure SetOutput(Var NewOut : TextFile);
  Procedure PushEntry(NewEntry : TEntry);

  Procedure WriteObjects;

Implementation

  Var
    CurIndex   : Integer = -1; { Dynamic arrays start at 0, so this must be before 0 }
    EntryArray : Array of TEntry;
    CFile : TextFile;

  Procedure SetOutput(Var NewOut : TextFile);
  Begin
    CFile := NewOut;
  End;

  Procedure PushEntry(NewEntry : TEntry);
  Begin

    { Check stack length }
    If (CurIndex >= High(EntryArray)) Then
      If Length(EntryArray) = 0 Then 
        SetLength(EntryArray, 1)
      Else
        SetLength(EntryArray, 2 * Length(EntryArray));

    { Push NewEntry to stack }
    Inc(CurIndex);
    EntryArray[CurIndex] := NewEntry;
  End;

  Procedure WriteObjects;
  Var 
    CEntry   : TEntry;
    I        : Integer;
    JS_Outer : TJsonObject;
    JS_Main  : TJsonArray;

  Begin
    JS_Outer := TJsonObject.Create;
    JS_Main  := TJsonArray.Create;

    For I:=0 To CurIndex Do
      Begin
      CEntry := EntryArray[I];
      JS_Main.Add( CEntry.ToJson );
      End;

    JS_Outer.Add('items', JS_Main);
    WriteLn(CFile, JS_Outer.FormatJson);
    Flush(CFile);
    JS_Outer.Destroy;
  End;

Initialization
  SetLength(EntryArray, 0);

End.
