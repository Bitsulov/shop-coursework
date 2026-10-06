import { combineSlices } from "@reduxjs/toolkit";
import { userSlice } from "entities/user";

export const rootReducer = combineSlices(userSlice);
