import { combineSlices } from "@reduxjs/toolkit";
import { userSlice } from "entities/user";
import { toastSlice } from "features/toast";

export const rootReducer = combineSlices(userSlice, toastSlice);
