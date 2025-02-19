;------------------------------------------------
        ;
        ;              S P I R O G R A P H S
        ;
        ;------------------------------------------------
        
        
        Const SCREEN_WIDTH   = 640
        Const SCREEN_HEIGHT  = 480
        Const SCREEN_MODE    =   2
        
        
        Const BORDER         = 48      ; Size of screen border.
        
        Const NUMBER_STARS   = 250
        
        
        
        Dim aryStarX#( NUMBER_STARS )
        Dim aryStarY#( NUMBER_STARS )
        Dim aryStarSpeedX#( NUMBER_STARS )
        
        Dim aryMatrix#( 3, 3 )         ; Rotation matrix.
        
        
        
        Global gintOuterRadius
        Global gintInnerRadius
        Global gfltResetO#
        Global gfltIncO#
        Global gintShapeLoops
        Global gfltZoomAngle#
        Global gbooDrawAsDots
        
        
        Global gintShapeRed1
        Global gintShapeGreen1
        Global gintShapeBlue1
        Global gintShapeRed2
        Global gintShapeGreen2
        Global gintShapeBlue2
        
        Global gfltRoll#                   ; Current roll angle of shape.
        Global gfltRollSpeed#              ; Current roll speed of shape.
        Global gintRollDirection           ; Direction of roll.
        
        Global gintBarR                    ; Rainbow line red colour component.
        Global gintBarG                    ; Rainbow line green colour component.
        Global gintBarB                    ; Rainbow line blue colour component.
        
        
        
        
        Initialise()
        DoEffect()
        End
        
        
        
        
        ;-----------------------------------------------------------------
        ; NAME      : CreateStarfield()
        ; PURPOSE   : Create a random starfield.
        ; INPUTS    : None.
        ; RETURNS   : Nothing.
        ;-----------------------------------------------------------------
        Function CreateStarfield()
        
                For intStar = 1 To NUMBER_STARS
                        aryStarX( intStar )                 = Rand( 0, SCREEN_WIDTH-1 )
                        aryStarY( intStar )                 = Rand( BORDER, SCREEN_HEIGHT-BORDER-1 )
                        aryStarSpeedX( intStar )        = Rnd( 1,8 )
                Next
        
        End Function
        
        
        
        ;-----------------------------------------------------------------
        ; NAME      : DoEffect()
        ; PURPOSE   : Main effect loop.
        ; INPUTS    : None.
        ; RETURNS   : Nothing.
        ;-----------------------------------------------------------------
        Function DoEffect()
        
                booRotate = False
                intShapeCounter = 1
        
                Repeat
        
                        Cls
        
                        booNewShape = UpdateShape( booRotate )
                        If booNewShape Then
                                intShapeCounter = intShapeCounter + 1
                                ;
                                ; Begin rotations after showing 3 shapes.
                                ;
                                If intShapeCounter > 3 Then
                                        booRotate = True
                                End If
                        End If
                
                        UpdateStarfield()
        
                        DrawStarfield()
                        DrawShape( gbooDrawAsDots )
                        DrawRainbowLines( 1, 2, 3, $000040 )
        
                        Flip
                
                Until KeyHit(1)
        
        End Function
        
        
        
        ;-----------------------------------------------------------------
        ; NAME      : DrawRainbowLines()
        ; PURPOSE   : Draws the screen border & colour-cycling lines.
        ; INPUTS    : intRedInc        - Increase red colour by.
        ;             intGreenInc      - Increase green colour by.
        ;             intBlueInc       - Increase blue colour by.
        ;             intBorderColour  - Colour of screen border.
        ; RETURNS   : Nothing.
        ;-----------------------------------------------------------------
        Function DrawRainbowLines( intRedInc, intGreenInc, intBlueInc, intBorderColour )
        
                ;
                ; Cycle the colours of the rainbow lines.
                ;
                gintBarR = gintBarR + intRedInc
                gintBarG = gintBarG + intGreenInc
                gintBarB = gintBarB + intBlueinc
        
                ;
                ; Draw border.
                ;
                Color 0, 0, intBorderColour
                Rect 0, 0, SCREEN_WIDTH, BORDER
                Rect 0, SCREEN_HEIGHT-BORDER, SCREEN_WIDTH, BORDER
        
                LockBuffer()
                For intY = 1 To 3
                        For intX = 0 To SCREEN_WIDTH-1
                                intColour = Abs(Sin(gintBarR+intX/4+intY*8)*255 Shl 16) + Abs(Sin(gintBarG+intX/3+intY*8)*255 Shl 8) +         Abs(Sin(gintBarB+intX/5+intY*8)*255)
                                ;
                                ; Draw top rainbow line.
                                ;
                                WritePixelFast intX, BORDER-intY, intColour
                                ;
                                ; Draw bottom rainbow line.
                                ;
                                WritePixelFast SCREEN_WIDTH-1-intX, SCREEN_HEIGHT-BORDER+intY, intColour
                        Next
                Next
                UnlockBuffer()
        
        End Function
        
        
        
        ;-----------------------------------------------------------------
        ; NAME      : DrawShape()
        ; PURPOSE   : Draws the spirograph shape on the screen.
        ; INPUTS    : booUseDots   - Draw as dots (true) or lines (false).
        ; RETURNS   : Nothing.
        ;-----------------------------------------------------------------
        Function DrawShape( booUseDots=False )        
                
                fltO# = gfltResetO
        
        
                LockBuffer()
                For intRepeat = 0 To gintShapeLoops
        
                        ;
                        ; Calculate the colour to draw pixels in this loop.
                        ;
                        fltFadePos# = Float intRepeat / gintShapeLoops
                        fltFadeNeg# = Float 1 - fltFadePos
        
                        intRed   = (gintShapeRed1   * fltFadePos)  +  (gintShapeRed2   * fltFadeNeg)
                        intGreen = (gintShapeGreen1 * fltFadePos)  +  (gintShapeGreen2 * fltFadeNeg)
                        intBlue  = (gintShapeBlue1  * fltFadePos)  +  (gintShapeBlue2  * fltFadeNeg)
        
                        intColour = intRed Shl 16 + intGreen Shl 8 + intBlue
        
        
                        fltO = fltO + gfltIncO
        
                        For intAngle = 0 To 360
        
                                ;
                                ; Calculate point position.
                                ;
                                intX = (gintOuterRadius+gintInnerRadius)*Cos(intAngle) -         (gintInnerRadius+fltO)*Cos(((gintOuterRadius+gintInnerRadius)/gintInnerRadius)*intAngle)
                                intY = (gintOuterRadius+gintInnerRadius)*Sin(intAngle) -         (gintInnerRadius+fltO)*Sin(((gintOuterRadius+gintInnerRadius)/gintInnerRadius)*intAngle)
        
                                ;
                                ; Apply rotation to point.
                                ;
                                intDrawX = SCREEN_WIDTH/2  + (aryMatrix(1,1) * intX + aryMatrix(1,2) * intY ) * Abs(Cos(gfltZoomAngle))
                                intDrawY = SCREEN_HEIGHT/2 + (aryMatrix(2,1) * intX + aryMatrix(2,2) * intY ) * Abs(Cos(gfltZoomAngle))
        
        
                                If booUseDots Then
                                        ;
                                        ; Draw the point.
                                        ;
                                        If intDrawX > 0 Then
                                                If intDrawY > 0 Then
                                                        If intDrawX < SCREEN_WIDTH-1
                                                                If intDrawY < SCREEN_HEIGHT-1 Then
                                                                        WritePixelFast intDrawX  , intDrawY  , intColour
                                                                        WritePixelFast intDrawX+1, intDrawY  , intColour
                                                                        WritePixelFast intDrawX  , intDrawY+1, intColour
                                                                        WritePixelFast intDrawX+1, intDrawY+1, intColour
                                                                End If
                                                        End If
                                                End If
                                        End If
                                Else
                                        ;
                                        ; Draw as lines.
                                        ;
                                        If intAngle > 0 Then
                                                Color 0, 0, intColour
                                                Line intOldX, intOldY, intDrawX, intDrawY
                                        End If
                                        intOldX = intDrawX
                                        intOldY = intDrawY
                                End If
        
                        Next
                
                Next
                UnlockBuffer()
        
        End Function
        
        
        
        ;-----------------------------------------------------------------
        ; NAME      : DrawStarfield()
        ; PURPOSE   : Draws the starfield on the screen.
        ; INPUTS    : None.
        ; RETURNS   : Nothing.
        ;-----------------------------------------------------------------
        Function DrawStarfield()
        
                LockBuffer()
                For intStar = 1 To NUMBER_STARS
                        intBright = aryStarSpeedX(intStar) * 255 / 8
                        WritePixelFast aryStarX(intStar), aryStarY(intStar), intBright Shl 16 + intBright Shl 8 + intBright
                Next
                UnlockBuffer()
        
        End Function
        
        
        
        ;-----------------------------------------------------------------
        ; NAME      : GenerateNewShape()
        ; PURPOSE   : Creates a new random spirograph shape.
        ; INPUTS    : None.
        ; RETURNS   : Nothing.
        ;-----------------------------------------------------------------
        Function GenerateNewShape()
        
                ;
                ; Generate new shape.
                ;
                gfltIncO        = Rnd( 1,  8 )
                gfltResetO        = Rnd( 1, 20 )
        
                gintOuterRadius = Rand( 30, 100 )
                gintInnerRadius = Rand(  5,  20 )
        
                ;
                ; How many iterations?
                ;
                gintShapeLoops = Rand(2,15)
        
                ;
                ; Choose new rotation speed.
                ;
                gintRollDirection = -gintRollDirection
                gfltRollSpeed = Rnd(1,3) * gintRollDirection
        
                ;
                ; Read next colour scheme from DATA statements.
                ;
                Read intFirstColour, intSecondColour
                If intFirstColour = -1 Then
                        Restore ShapeColours
                        Read intFirstColour, intSecondColour
                End If
        
                ;
                ; Split colours their separate RGB components.
                ;
                gintShapeRed1   = intFirstColour Shr 16 And $ff
                gintShapeGreen1 = intFirstColour Shr  8 And $ff
                gintShapeBlue1  = intFirstColour        And $ff
        
                gintShapeRed2   = intSecondColour Shr 16 And $ff
                gintShapeGreen2 = intSecondColour Shr  8 And $ff
                gintShapeBlue2  = intSecondColour        And $ff
        
                ;
                ; Switch between using dots & lines.
                ;
                gbooDrawAsDots = Not gbooDrawAsDots
        
        End Function
        
        
        
        ;-----------------------------------------------------------------
        ; NAME      : Initialise()
        ; PURPOSE   : Opens the screen & initialises the effect.
        ; INPUTS    : None.
        ; RETURNS   : Nothing.
        ;-----------------------------------------------------------------
        Function Initialise()
        
                Graphics SCREEN_WIDTH, SCREEN_HEIGHT, 0, SCREEN_MODE
                SetBuffer BackBuffer()
        
                SeedRnd MilliSecs()
        
                ;
                ; Generate starfield.
                ;
                CreateStarfield()
        
                ;
                ; Create first shape.
                ;
                GenerateNewShape()
        
                ;
                ; Initialise variables.
                ;
                gintRollDirection =  1
                gfltZoomAngle     = 90
                gbooDrawAsDots    = False
        
        End Function
        
        
        
        ;-----------------------------------------------------------------
        ; NAME      : PopulateMatrix()
        ; PURPOSE   : Applies rotation angles to the rotation matrix.
        ; INPUTS    : fltRoll    - Roll angle.
        ;             fltTilt    - Title angle.
        ;             fltTurn    - Turn angle.
        ; RETURNS   : None.
        ;-----------------------------------------------------------------
        Function PopulateMatrix( fltRoll#, fltTilt#, fltTurn# )
        
                A#=Cos( fltTilt )
                B#=Sin( fltTilt )
                C#=Cos( fltTurn )
                D#=Sin( fltTurn )
                E#=Cos( fltRoll )
                F#=Sin( fltRoll )
        
                AD#=A*D
                BD#=B*D
        
                aryMatrix( 1, 1 )= C*E
                aryMatrix( 2, 1 )=-C*F
                aryMatrix( 3, 1 )= D
                aryMatrix( 1, 2 )= BD*E + A*F
                aryMatrix( 2, 2 )=-BD*F + A*E
                aryMatrix( 3, 2 )=-B*C
                aryMatrix( 1, 3 )=-AD*E + B*F
                aryMatrix( 2, 3 )= AD*F + B*E
                aryMatrix( 3, 3 )= A*C
                
        End Function
        
        
        
        ;-----------------------------------------------------------------
        ; NAME      : UpdateShape()
        ; PURPOSE   : Updates the spirograph shape and applies any
        ;             scaling & rotation to it.
        ; INPUTS    : booRotate    - Perform rotations?   True/False.
        ; RETURNS   : This function returns true if a new shape has been
        ;             selected or false if the last shape is still being used.
        ;-----------------------------------------------------------------
        Function UpdateShape( booRotate )
        
                booNewShape = False
        
                ;
                ; Update shape zoom.
                ;
                gfltZoomAngle = gfltZoomAngle + 1
                If gfltZoomAngle >= 270 Then
                        gfltZoomAngle = 90
                End If
        
                ;
                ; Is it time to create a new shape ?
                ;
                If gfltZoomAngle=90 Then
                        GenerateNewShape()
                        booNewShape = True
                End If
        
                ;
                ; Update rotation.
                ;
                If booRotate Then
                        gfltRoll = gfltRoll + gfltRollSpeed        
                        If gfltRoll >= 360 Then
                                gfltRoll = gfltRoll - 360
                        End If
                        PopulateMatrix( gfltRoll, 0, 0 )
                Else
                        PopulateMatrix( 0, 0, 0)
                End If
        
                ;
                ; Return whether a new spirograph shape has been generated.
                ;
                Return booNewShape
        
        End Function
        
        
        
        ;-----------------------------------------------------------------
        ; NAME      : UpdateStarfield()
        ; PURPOSE   : Updates the position of all the stars in starfield.
        ; INPUTS    : None.
        ; RETURNS   : Nothing.
        ;-----------------------------------------------------------------
        Function UpdateStarfield()
        
                For intStar = 1 To NUMBER_STARS
                        aryStarX(intStar) = aryStarX(intStar) + aryStarSpeedX(intStar)
                        If aryStarX(intStar)>=SCREEN_WIDTH Then
                                aryStarX(intStar) = aryStarX(intStar) - SCREEN_WIDTH
                        End If
                Next
        
        End Function
        
        
        
        
.ShapeColours
        ;-------------------------------
        ;     FADE      FADE
        ;     FROM       TO
        ;-------------------------------
        Data $ffffff, $000000
        Data $ff0000, $ffff00
        Data $00ff00, $008800
        Data $ff0000, $0000ff
        Data $0000ff, $00ffff
        Data $ff00ff, $ffff00
        Data -1     , -1


;~IDEal Editor Parameters:
;~C#Blitz3D